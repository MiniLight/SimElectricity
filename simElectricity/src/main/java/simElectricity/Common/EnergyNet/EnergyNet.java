package simElectricity.Common.EnergyNet;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import simElectricity.API.EnergyTile.*;
import simElectricity.API.EnergyTile.ITransformer.ITransformerWinding;
import simElectricity.API.IEnergyNetUpdateHandler;
import simElectricity.API.Util;
import simElectricity.Common.ConfigManager;

import java.util.*;

public final class EnergyNet {
    // private WeightedMultigraph<IBaseComponent, Resistor> tileEntityGraph =
    // new WeightedMultigraph<IBaseComponent, Resistor>(Resistor.class);
    private SimpleGraph<IBaseComponent, DefaultEdge> tileEntityGraph = new SimpleGraph<IBaseComponent, DefaultEdge>(DefaultEdge.class);
    public Map<IBaseComponent, Float> voltageCache = new HashMap<IBaseComponent, Float>();
    /**
     * A flag for recalculate the energynet
     */
    private boolean calc = false;

    //Optimization--------------------------------------------------------------------
    private boolean nodeIsLine(IBaseComponent conductor, SimpleGraph<IBaseComponent, DefaultEdge> optimizedTileEntityGraph) {
        if (conductor.getClass() == VirtualConductor.class)
            return false;
        if (!(conductor instanceof IConductor))
            return false;
        if (VirtualConductor.conductorInVirtual((IConductor) conductor))
            return false;

        List<IBaseComponent> list = Graphs.neighborListOf(optimizedTileEntityGraph, conductor);
        for (IBaseComponent iBaseComponent : list) {
            if (!(iBaseComponent instanceof IConductor))
                return false;
        }

        return list.size() == 2;
    }

    private VirtualConductor floodFill(IBaseComponent conductor, VirtualConductor virtualConductor, SimpleGraph<IBaseComponent, DefaultEdge> optimizedTileEntityGraph) {
        if (nodeIsLine(conductor, optimizedTileEntityGraph)) {
            if (virtualConductor == null)
                virtualConductor = new VirtualConductor();
            virtualConductor.append((IConductor) conductor);
            List<IBaseComponent> neighborList = Graphs.neighborListOf(optimizedTileEntityGraph, conductor);
            for (IBaseComponent iBaseComponent : neighborList) {
                floodFill(iBaseComponent, virtualConductor, optimizedTileEntityGraph);
            }
        } else if (virtualConductor != null) {
            virtualConductor.appendConnection(conductor);
        }

        return virtualConductor;
    }

    private boolean mergeIConductorNode(SimpleGraph<IBaseComponent, DefaultEdge> optimizedTileEntityGraph) {
        boolean result = false;
        VirtualConductor virtualConductor = null;

        Set<IBaseComponent> iBaseComponentSet = optimizedTileEntityGraph.vertexSet();
        for (IBaseComponent iBaseComponent : iBaseComponentSet) {
            virtualConductor = floodFill(iBaseComponent, virtualConductor, optimizedTileEntityGraph);

            if (virtualConductor != null) {
                break;
            }
        }

        if (virtualConductor != null) {
            optimizedTileEntityGraph.addVertex(virtualConductor);
            optimizedTileEntityGraph.addEdge(virtualConductor, virtualConductor.getConnection(0));
            optimizedTileEntityGraph.addEdge(virtualConductor, virtualConductor.getConnection(1));

            for (IConductor conductor : VirtualConductor.allConductorInVirtual())
                optimizedTileEntityGraph.removeVertex(conductor);

            result = true;
        }

        return result;
    }

    //Simulator------------------------------------------------------------------------
    @SuppressWarnings( { "StatementWithEmptyBody", "unchecked" })
    private void runSimulator() {
        SimpleGraph<IBaseComponent, DefaultEdge> optimizedTileEntityGraph = (SimpleGraph<IBaseComponent, DefaultEdge>) tileEntityGraph.clone();

        //try to optimization
        if (ConfigManager.optimizeNodes) {
            System.out.printf("raw:%d nodes\n", optimizedTileEntityGraph.vertexSet().size());
            VirtualConductor.mapClear();
            while (mergeIConductorNode(optimizedTileEntityGraph)) ;
            System.out.printf("optimized:%d nodes\n", optimizedTileEntityGraph.vertexSet().size());
        }


        List<IBaseComponent> unknownVoltageNodes = new ArrayList<IBaseComponent>();
        unknownVoltageNodes.addAll(optimizedTileEntityGraph.vertexSet());

        int matrixSize = unknownVoltageNodes.size();

        double[][] A = new double[matrixSize][matrixSize]; //A initialized 0 matrix
        double[] b = new double[matrixSize];

        for (int rowIndex = 0; rowIndex < matrixSize; rowIndex++) {
            IBaseComponent currentRowComponent = unknownVoltageNodes.get(rowIndex);

            //Add fixed voltage sources
            if (currentRowComponent instanceof ICircuitComponent) {
                //Possible voltage source, getOutputVoltage()=0 for sinks, getOutputVoltage>0 for sources
                b[rowIndex] = ((ICircuitComponent) currentRowComponent).getOutputVoltage()
                        / currentRowComponent.getResistance();
            } else {
                //Normal conductor nodes
                b[rowIndex] = 0;
            }


            List<IBaseComponent> neighborList = Graphs.neighborListOf(optimizedTileEntityGraph, currentRowComponent);
            //Generate row for conductor node
            if (currentRowComponent instanceof IConductor || currentRowComponent instanceof IManualJunction) {
                for (int columnIndex = 0; columnIndex < matrixSize; columnIndex++) {
                    double cellData = 0;

                    if (rowIndex == columnIndex) { //Key cell
                        //Add neighbor resistance
                        for (IBaseComponent neighbor : neighborList) {
                            if (neighbor instanceof IConductor || neighbor instanceof IManualJunction) {
                                cellData += 2.0D / (currentRowComponent.getResistance() + neighbor.getResistance());  // IConductor next to IConductor
                            } else {
                                cellData += 2.0D / currentRowComponent.getResistance();                              // IConductor next to other components
                            }
                        }
                    } else {
                        IBaseComponent currentColumnComponent = unknownVoltageNodes.get(columnIndex);
                        if (neighborList.contains(currentColumnComponent)) {
                            if (currentColumnComponent instanceof IConductor || currentColumnComponent instanceof IManualJunction) {
                                cellData = -2.0D / (currentRowComponent.getResistance() + currentColumnComponent.getResistance());
                            } else {
                                cellData = -2.0D / currentRowComponent.getResistance();
                            }
                        }

                    }

                    A[rowIndex][columnIndex] = cellData;
                }
            } else { //For other nodes (can only have a IConductor neighbor or no neighbor!)
                //Find the only possible neighbor (maybe not exist)
                IConductor neighbor = null;
                for (IBaseComponent possibleNeighbor : neighborList) {
                    if (possibleNeighbor != null)
                        neighbor = (IConductor) possibleNeighbor; //Must be a IConductor!
                }

                for (int columnIndex = 0; columnIndex < matrixSize; columnIndex++) {
                    double cellData = 0;

                    if (rowIndex == columnIndex) { //Key cell
                        if (neighbor != null) {
                            cellData += 2.0D / neighbor.getResistance();
                        }

                        //Add internal resistance for fixed voltage sources
                        if (currentRowComponent instanceof ICircuitComponent) {
                            cellData += 1.0 / currentRowComponent.getResistance();
                        } else if (currentRowComponent instanceof ITransformerWinding) {
                            ITransformerWinding winding = (ITransformerWinding) currentRowComponent;
                            if (winding.isPrimary()) {
                                cellData += winding.getRatio() * winding.getRatio() / winding.getResistance();
                            } else {
                                cellData += 1.0 / winding.getResistance();
                            }
                        }
                    } else {
                        if (neighbor == unknownVoltageNodes.get(columnIndex)) {
                            cellData = -2.0D / neighbor.getResistance();
                        } else if (currentRowComponent instanceof ITransformerWinding) { //Add transformer association
                            IBaseComponent currentColumnComponent = unknownVoltageNodes.get(columnIndex);
                            ITransformerWinding winding = (ITransformerWinding) currentRowComponent;
                            ITransformer core = winding.getCore();

                            if ((winding.isPrimary() && core.getSecondary() == currentColumnComponent) ||
                                    ((!winding.isPrimary()) && core.getPrimary() == currentColumnComponent))
                                cellData = -winding.getRatio() / winding.getResistance();
                        }
                    }

                    A[rowIndex][columnIndex] = cellData;
                }
            }


        }

        float[] x = MatrixOperation.lsolve(A, b);

        voltageCache.clear();
        for (int i = 0; i < x.length; i++) {
            voltageCache.put(unknownVoltageNodes.get(i), x[i]);
        }
    }
    /*End of Simulator*/


    /**
     * Called in each tick to attempt to do calculation
     */
    public static void onTick(World world) {
        EnergyNet energyNet = getForWorld(world);
        //energyNet.calc = true;
        if (energyNet.calc) {
            energyNet.calc = false;
            energyNet.runSimulator();

            //Check power distribution
            try {
                for (IBaseComponent tile : energyNet.tileEntityGraph.vertexSet()) {
                    if (tile instanceof IEnergyNetUpdateHandler) {
                        ((IEnergyNetUpdateHandler) tile).onEnergyNetUpdate();
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    //Editing of the jGraph--------------------------------------------------------------------------------

    /**
     * Internal use only, return a list containing neighbor TileEntities (Just for IBaseComponent)
     */
    private static List<IBaseComponent> neighborListOf(TileEntity te) {
        List<IBaseComponent> result = new ArrayList<IBaseComponent>();
        TileEntity temp;

        if (te instanceof IConductor) {
            for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
                temp = Util.getTileEntityonDirection(te, direction);
                if (temp instanceof IConductor) {  //Conductor
                    result.add((IConductor) temp);
                } else if (temp instanceof IEnergyTile) {   //IEnergyTile
                    if (((IEnergyTile) temp).getFunctionalSide() == direction.getOpposite())
                        result.add((IEnergyTile) temp);
                } else if (temp instanceof IComplexTile) {  //IComplexTile
                    if (((IComplexTile) temp).getCircuitComponent(direction.getOpposite()) != null)
                        result.add(((IComplexTile) temp).getCircuitComponent(direction.getOpposite()));
                } else if (temp instanceof ITransformer) {
                    if (((ITransformer) temp).getPrimarySide() == direction.getOpposite())
                        result.add(((ITransformer) temp).getPrimary());

                    if (((ITransformer) temp).getSecondarySide() == direction.getOpposite())
                        result.add(((ITransformer) temp).getSecondary());
                } else if (temp instanceof IManualJunction) {
                    result.add((IManualJunction) temp);
                }
            }
        }


        if (te instanceof IEnergyTile) {
            temp = Util.getTileEntityonDirection(te, ((IEnergyTile) te).getFunctionalSide());

            if (temp instanceof IConductor) {
                result.add((IBaseComponent) temp);
            }
        }

        if (te instanceof IManualJunction) {
            ((IManualJunction) te).addNeighbors(result);
        }

        return result;
    }

    /**
     * Add a TileEntity to the energynet
     */
    public void addTileEntity(TileEntity te) {
        if (te instanceof IComplexTile) {      //IComplexTile
            IComplexTile ct = ((IComplexTile) te);

            ICircuitComponent SubComponent;
            TileEntity neighbor;

            for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
                SubComponent = ct.getCircuitComponent(direction);

                if (SubComponent instanceof IBaseComponent) {
                    if (!tileEntityGraph.containsVertex(SubComponent))    //If the subComponent haven't been added, add it!
                        tileEntityGraph.addVertex(SubComponent);

                    neighbor = Util.getTileEntityonDirection(te, direction);

                    if (neighbor instanceof IConductor) {                //Connected properly
                        if (!tileEntityGraph.containsVertex((IConductor) neighbor))
                            tileEntityGraph.addVertex((IConductor) neighbor);
                        tileEntityGraph.addEdge(SubComponent, (IConductor) neighbor); //Add association
                    }
                }
            }

        } else if (te instanceof ITransformer) {         //Transformer
            ITransformer transformer = ((ITransformer) te);
            ITransformerWinding primary = ((ITransformer) te).getPrimary();
            ITransformerWinding secondary = ((ITransformer) te).getSecondary();

            TileEntity neighbor;

            //Add primary
            if (!tileEntityGraph.containsVertex(primary))
                tileEntityGraph.addVertex(primary);

            neighbor = Util.getTileEntityonDirection(te, transformer.getPrimarySide());
            if (neighbor instanceof IConductor) {
                if (!tileEntityGraph.containsVertex((IBaseComponent) neighbor))
                    tileEntityGraph.addVertex((IBaseComponent) neighbor);
                tileEntityGraph.addEdge(primary, (IBaseComponent) neighbor);
            }

            //Add secondary
            if (!tileEntityGraph.containsVertex(secondary))
                tileEntityGraph.addVertex(secondary);

            neighbor = Util.getTileEntityonDirection(te, transformer.getSecondarySide());
            if (neighbor instanceof IConductor) {
                if (!tileEntityGraph.containsVertex((IBaseComponent) neighbor))
                    tileEntityGraph.addVertex((IBaseComponent) neighbor);
                tileEntityGraph.addEdge(secondary, (IBaseComponent) neighbor);
            }

        } else {  //IBaseComponent and IConductor
            List<IBaseComponent> neighborList = neighborListOf(te);

            if (!tileEntityGraph.containsVertex((IBaseComponent) te))
                tileEntityGraph.addVertex((IBaseComponent) te);

            for (IBaseComponent tileEntity : neighborList) {
                if (!tileEntityGraph.containsVertex(tileEntity))
                    tileEntityGraph.addVertex(tileEntity);
                tileEntityGraph.addEdge((IBaseComponent) te, tileEntity);
            }
        }
        calc = true;
    }

    /**
     * Remove a TileEntity from the energy net
     */
    public void removeTileEntity(TileEntity te) {
        if (te instanceof IComplexTile) { //For a complexTile every subComponents has to be removed!
            ICircuitComponent[] SubComponents = new ICircuitComponent[6];
            SubComponents[0] = ((IComplexTile) te).getCircuitComponent(ForgeDirection.NORTH);
            SubComponents[1] = ((IComplexTile) te).getCircuitComponent(ForgeDirection.SOUTH);
            SubComponents[2] = ((IComplexTile) te).getCircuitComponent(ForgeDirection.EAST);
            SubComponents[3] = ((IComplexTile) te).getCircuitComponent(ForgeDirection.WEST);
            SubComponents[4] = ((IComplexTile) te).getCircuitComponent(ForgeDirection.UP);
            SubComponents[5] = ((IComplexTile) te).getCircuitComponent(ForgeDirection.DOWN);

            for (ICircuitComponent subComponent : SubComponents) {
                if (subComponent instanceof IBaseComponent)
                    tileEntityGraph.removeVertex(subComponent);
            }
        } else if (te instanceof ITransformer) {
            tileEntityGraph.removeVertex(((ITransformer) te).getPrimary());
            tileEntityGraph.removeVertex(((ITransformer) te).getSecondary());
        } else {  //IBaseComponent and IConductor
            tileEntityGraph.removeVertex((IBaseComponent) te);
        }
        calc = true;
    }

    /**
     * Refresh a node information for a tile which ALREADY attached to the energy network
     */
    public void rejoinTileEntity(TileEntity te) {
        removeTileEntity(te);
        addTileEntity(te);
    }

    /**
     * Mark the energy net for updating in next tick
     */
    public void markForUpdate(TileEntity te) {
        calc = true;
    }

    /**
     * Return a instance of energynet for a specific world
     */
    public static EnergyNet getForWorld(World world) {
        WorldData worldData = WorldData.get(world);
        return worldData.energyNet;
    }

    /**
     * Creation of the energy network
     */
    public EnergyNet() {
        System.out.println("EnergyNet create");
    }
}
