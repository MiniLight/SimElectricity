package simelectricity.energynet;

import simelectricity.api.node.ISEGridNode;
import simelectricity.energynet.components.GridNode;

public abstract class GridEvent extends EnergyEventBase {
    protected final ISEGridNode node1;

    private GridEvent(int priority, ISEGridNode node1) {
        super(priority);
        this.node1 = node1;
    }

    public static class AppendNode extends GridEvent {
        public AppendNode(ISEGridNode node) {
            super(1, node);
        }

        @Override
        public void process(EnergyNetDataProvider dataProvider) {
            this.needUpdate = true;
            this.changedStructure = true;
            dataProvider.addGridNode((GridNode) this.node1);
        }
    }

    public static class RemoveNode extends GridEvent {
        public RemoveNode(ISEGridNode node) {
            super(3, node);
        }

        @Override
        public void process(EnergyNetDataProvider dataProvider) {
            this.needUpdate = true;
            this.changedStructure = true;
            dataProvider.removeGridNode((GridNode) this.node1);
        }
    }

    public static class Connect extends GridEvent {
        protected final ISEGridNode node2;
        protected final double resistance;

        public Connect(ISEGridNode node1, ISEGridNode node2, double resistance) {
            super(2, node1);
            this.node2 = node2;
            this.resistance = resistance;
        }

        @Override
        public void process(EnergyNetDataProvider dataProvider) {
            this.needUpdate = true;
            this.changedStructure = true;
            dataProvider.addGridConnection((GridNode) this.node1, (GridNode) this.node2, this.resistance);
        }
    }

    public static class BreakConnection extends GridEvent {
        protected final ISEGridNode node2;

        public BreakConnection(ISEGridNode node1, ISEGridNode node2) {
            super(2, node1);
            this.node2 = node2;
        }

        @Override
        public void process(EnergyNetDataProvider dataProvider) {
            this.needUpdate = true;
            this.changedStructure = true;
            dataProvider.removeGridConnection((GridNode) this.node1, (GridNode) this.node2);
        }
    }

    public static class MakeTransformer extends GridEvent {
        protected final ISEGridNode sec;
        protected final double resistance, ratio;

        public MakeTransformer(ISEGridNode pri, ISEGridNode sec, double resistance, double ratio) {
            super(2, pri);
            this.sec = sec;
            this.resistance = resistance;
            this.ratio = ratio;
        }

        @Override
        public void process(EnergyNetDataProvider dataProvider) {
            this.needUpdate = true;
            dataProvider.makeTransformer((GridNode) this.node1, (GridNode) this.sec, this.ratio, this.resistance);
        }
    }

    public static class BreakTranformer extends GridEvent {
        public BreakTranformer(ISEGridNode node) {
            super(2, node);
        }

        @Override
        public void process(EnergyNetDataProvider dataProvider) {
            this.needUpdate = true;
            dataProvider.breakTransformer((GridNode) this.node1);
        }
    }

}
