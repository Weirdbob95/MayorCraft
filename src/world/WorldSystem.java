package world;

import core.*;
import graphics.Graphics;
import static org.lwjgl.opengl.GL11.*;

public class WorldSystem extends AbstractSystem {

    private WorldComponent world;

    public WorldSystem(WorldComponent world) {
        this.world = world;
    }

    private void drawBorders() {
        Color4d.BLACK.glColor();
        glLineWidth(1);
        for (Edge e : world.edges) {
            if (e.inView(Main.gameManager.rmc)) {
                if ((e.water == 0 || !e.isLand) && (e.p0.isLand || e.p1.isLand)) {
                    {
                        glBegin(GL_LINE_STRIP);
                        {
                            for (int i = 0; i < e.noisePath.size(); i++) {
                                e.noisePath.get(i).glVertex();
                                i += Math.max(0, Main.gameManager.rmc.zoom / 5);
                            }
                            if (Main.gameManager.rmc.zoom >= 5) {
                                e.noisePath.get(e.noisePath.size() - 1).glVertex();
                            }
                        }
                        glEnd();
                    }
                }
            }
        }
    }

    private void drawLand() {
        for (Center c : world.centers) {
            if (c.inView(Main.gameManager.rmc)) {
                if (c.isLand) {
                    c.color.glColor();
                    for (Edge e : c.borders) {
                        glBegin(GL_TRIANGLE_FAN);
                        {
                            c.pos.glVertex();
                            for (int i = 0; i < e.noisePath.size(); i++) {
                                e.noisePath.get(i).glVertex();
                                i += Math.max(0, Main.gameManager.rmc.zoom / 5);
                            }
                            if (Main.gameManager.rmc.zoom >= 5) {
                                e.noisePath.get(e.noisePath.size() - 1).glVertex();
                            }
                        }
                        glEnd();
                    }
                }
            }
        }
    }

    private void drawRivers() {
        for (Edge e : world.edges) {
            if (e.inView(Main.gameManager.rmc)) {
                if (e.water > 0 && e.isLand) {
                    //Draw river
                    Graphics.fillEllipse(e.v0.pos, new Vec2(World.riverWidth(e.water), World.riverWidth(e.water)), World.waterColor(e.v0.elevation), 10);
                    glBegin(GL_TRIANGLE_STRIP);
                    {
                        //Main part
                        Vec2 dir = e.v1.pos.subtract(e.v0.pos);
                        for (int i = 0; i < e.noisePath.size() - 2; i++) {
                            Vec2 start = e.noisePath.get(i);
                            Vec2 end = e.noisePath.get(i + 1);
                            Vec2 side = end.subtract(start).setLength(World.riverWidth(e.water)).normal();
                            start.add(side).glVertex();
                            start.add(side.reverse()).glVertex();
                            double perc = (end.dot(dir) - e.v0.pos.dot(dir)) / dir.lengthSquared();
                            World.waterColor(e.v0.elevation * (1 - perc) + e.v1.elevation * perc).glColor();
                            //waterColor(e.v0.elevation * (1 - (double) i / e.noisePath.size()) + e.v1.elevation * ((double) i / e.noisePath.size())).glColor();
                            end.add(side).glVertex();
                            end.add(side.reverse()).glVertex();
                            i += Math.max(0, Main.gameManager.rmc.zoom / 10);
                        }
                        //Blend with next river
                        Vec2 start = e.noisePath.get(e.noisePath.size() - 2);
                        Vec2 end = e.noisePath.get(e.noisePath.size() - 1);
                        Vec2 side = end.subtract(start).setLength(World.riverWidth(e.water)).normal();
                        start.add(side).glVertex();
                        start.add(side.reverse()).glVertex();
                        World.waterColor(e.v1.elevation).glColor();
                        if (e.v1.isCoast) {
                            end.add(side).glVertex();
                            end.add(side.reverse()).glVertex();
                        } else {
                            Edge ed = e.v1.edgeTo(e.v1.downslope);
                            if (e.v1.downslope == e.v0) {
                                System.out.println(e.v1.elevation - e.v0.elevation);
                            }
                            Vec2 side1 = end.subtract(start).setLength(World.riverWidth(ed.water)).normal();
                            end.add(side1).glVertex();
                            end.add(side1.reverse()).glVertex();
                            Vec2 start2 = ed.v0.pos;
                            Vec2 end2 = ed.noisePath.get(1);
                            Vec2 side2 = end2.subtract(start2).setLength(World.riverWidth(ed.water)).normal();
                            start2.add(side2).glVertex();
                            start2.add(side2.reverse()).glVertex();
                        }
                    }
                    glEnd();
                    Graphics.fillEllipse(e.v1.pos, new Vec2(World.riverWidth(e.water), World.riverWidth(e.water)), World.waterColor(e.v1.elevation), 10);

                }
            }
        }
    }

    private void drawWater() {
        for (Center c : world.centers) {
            if (!c.isLand) {
                if (c.elevation > 0) {
                    if (c.inView(Main.gameManager.rmc)) {
                        c.color.glColor();
                        for (Edge e : c.borders) {
                            glBegin(GL_TRIANGLE_FAN);
                            {
                                c.pos.glVertex();
                                for (int i = 0; i < e.noisePath.size(); i++) {
                                    e.noisePath.get(i).glVertex();
                                    i += Math.max(0, Main.gameManager.rmc.zoom / 5);
                                }
                                if (Main.gameManager.rmc.zoom >= 5) {
                                    e.noisePath.get(e.noisePath.size() - 1).glVertex();
                                }
                            }
                            glEnd();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void update() {
        glEnable(GL_LINE_SMOOTH);
        glDisable(GL_TEXTURE_2D);

        Graphics.fillRect(Main.gameManager.rmc.LL(), Main.gameManager.rmc.viewSize, World.waterColor(0));

        //Draw land
        drawLand();

        //Draw borders
        drawBorders();

        //Draw rivers
        drawRivers();

        //Draw water
        drawWater();
    }
}