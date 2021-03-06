package units;

import buildings.Building;
import buildings.BuildingType;
import static buildings.BuildingType.*;
import buildings.BuildingTypeComponent;
import core.AbstractSystem;
import core.Main;
import core.Sounds;
import movement.PositionComponent;
import world.GridPoint;
import world.Terrain;
import static world.Terrain.ROCK;
import static world.Terrain.TREE;
import world.TerrainComponent;
import world.World;

public class UnitSystem extends AbstractSystem {

    private PositionComponent pc;
    private DestinationComponent dc;
    private BuildingTypeComponent btc;
    private GatheringComponent gc;
    private AnimationComponent ac;

    public UnitSystem(PositionComponent pc, DestinationComponent dc, BuildingTypeComponent btc, GatheringComponent gc, AnimationComponent ac) {
        this.pc = pc;
        this.dc = dc;
        this.btc = btc;
        this.gc = gc;
        this.ac = ac;
    }

    public void AI(BuildingType type) {
        if (btc.type != null && atBuilding(SCHOOL)) {
            btc.type = SCHOOL;
            gc.carrying = false;
            ac.color = btc.type.color;
        }
        switch (type) {
            case BANK:
                if (atBuilding(BANK)) {
                    if (gc.timeRemaining <= 0) {
                        gc.timeRemaining = 300;
                        Main.gameManager.rc.money += 10;
                        Sounds.playSound("drop_the_clip_1.wav", false, Math.min(1, 500. / pc.pos.subtract(Main.gameManager.rmc.viewPos).length()));
                    }
                    gc.timeRemaining--;
                }
                break;
            case LABORATORY:
                if (atBuilding(LABORATORY)) {
                    if (gc.timeRemaining <= 0) {
                        gc.timeRemaining = 300;
                        if (Main.gameManager.rc.money >= 5) {
                            Main.gameManager.rc.science += 10;
                            Main.gameManager.rc.money -= 5;
                            Sounds.playSound("wglass_3_1.wav", false, Math.min(1, 500. / pc.pos.subtract(Main.gameManager.rmc.viewPos).length()));
                        }
                    }
                    gc.timeRemaining--;
                }
                break;
            case LUMBER_YARD:
                gather(LUMBER_YARD, TREE);
                break;
            case MINE:
                gather(MINE, ROCK);
                break;
            case SCHOOL:
                if (btc.type != null && dc.building != null && dc.building.getComponent(BuildingTypeComponent.class).type != HOUSE) {
                    btc.type = dc.building.getComponent(BuildingTypeComponent.class).type;
                    gc.carrying = false;
                    ac.color = btc.type.color;
                }
                break;
        }
    }

    private boolean atBuilding(BuildingType type) {
        return dc.building != null && dc.building.getComponent(BuildingTypeComponent.class).type == type;
    }

    private void gather(BuildingType type, Terrain terrain) {
        if (atBuilding(type)) {
            if (gc.carrying && gc.timeRemaining == 0) {
                gc.carrying = false;
                Main.gameManager.rc.materials += 200;
            }
            dc.terrain = nearest(terrain);
            if (dc.terrain != null) {
                dc.des = dc.terrain.toVec2();
                dc.changed = true;
            }
        } else if (dc.terrain != null) {
            if (dc.terrain.terrain == null) {
                dc.terrain = nearest(terrain);
                if (dc.terrain != null) {
                    dc.des = dc.terrain.toVec2();
                    dc.changed = true;
                }
            }
            if (dc.terrain != null && dc.terrain.terrain == terrain) {
                if (!gc.carrying) {
                    gc.timeRemaining = 155;
                    gc.carrying = true;
                } else {
                    if (gc.timeRemaining > 0) {
                        gc.timeRemaining--;
                        if (gc.timeRemaining % 30 == 0) {
                            Sounds.playSound("metal_box_1.wav", false, Math.min(1, 500. / pc.pos.subtract(Main.gameManager.rmc.viewPos).length()));
                        }
                        if (gc.timeRemaining == 0) {
                            dc.terrain.terrain = null;
                            dc.terrain.blocked = false;
                            Main.gameManager.elc.get(World.class).getComponent(TerrainComponent.class).terrainMap.get(terrain).remove(dc.terrain);
                            dc.terrain = null;
                            dc.building = nearest(type);
                            if (dc.building != null) {
                                dc.des = dc.building.getComponent(PositionComponent.class).pos;
                                dc.changed = true;
                            }
                        }
                    }
                }
            }
        }
    }

    public Building nearest(BuildingType type) {
        Building r = null;
        for (Building b : Main.gameManager.elc.getList(Building.class)) {
            if (b.getComponent(BuildingTypeComponent.class).type == type) {
                if ((r == null && b.getComponent(PositionComponent.class).pos.subtract(pc.pos).lengthSquared() < 5000000)
                        || (r != null && b.getComponent(PositionComponent.class).pos.subtract(pc.pos).lengthSquared() < r.getComponent(PositionComponent.class).pos.subtract(pc.pos).lengthSquared())) {
                    r = b;
                }
            }
        }
        return r;
    }

    public GridPoint nearest(Terrain t) {
        GridPoint r = null;
        for (GridPoint gp : Main.gameManager.elc.get(World.class).getComponent(TerrainComponent.class).terrainMap.get(t)) {
            if ((r == null && gp.toVec2().subtract(pc.pos).lengthSquared() < 4000000)
                    || (r != null && gp.toVec2().subtract(pc.pos).lengthSquared() < r.toVec2().subtract(pc.pos).lengthSquared())) {
                r = gp;
            }
        }
        return r;
    }

    @Override
    protected boolean pauseable() {
        return true;
    }

    @Override
    public void update() {
        if (dc.atDest) {
            if (btc.type == null) {
                for (BuildingType type : BuildingType.values()) {
                    AI(type);
                }
            } else {
                AI(btc.type);
            }
        }
    }
}
