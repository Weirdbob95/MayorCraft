package units;

import core.AbstractComponent;
import core.Main;
import core.Vec2;
import movement.PositionComponent;
import world.GridPoint;

public class SelectableComponent extends AbstractComponent {

    public double size;
    public PositionComponent pc;
    public DestinationComponent dc;

    public SelectableComponent(double size, PositionComponent pc) {
        this(size, pc, null);
    }

    public SelectableComponent(double size, PositionComponent pc, DestinationComponent dc) {
        this.size = size;
        this.pc = pc;
        this.dc = dc;
        Main.gameManager.sc.all.add(this);
    }

    @Override
    protected void destroy() {
        Main.gameManager.sc.all.remove(this);
        Main.gameManager.sc.selected.remove(this);
    }

    public boolean open(Vec2 pos) {
        for (GridPoint gp : Main.gameManager.gc.points(pos.subtract(new Vec2(size, size)), pos.add(new Vec2(size, size)))) {
            if (gp.blocked) {
                return false;
            }
        }
        return true;
    }
}
