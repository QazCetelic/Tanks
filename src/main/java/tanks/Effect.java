package tanks;

import tanks.bullet.Bullet;
import tanks.gui.screen.ScreenGame;
import tanks.obstacle.Obstacle;

public class Effect extends Movable implements IDrawableWithGlow
{
    public enum EffectType {fire, smokeTrail, trail, ray, mineExplosion, laser, piece, obstaclePiece, obstaclePiece3d, charge, tread, darkFire, electric, healing, stun, bushBurn, glow, teleporterLight, teleporterPiece, interfacePiece, snow, shield, boostLight, exclamation}

    public enum State {live, removed, recycle}

    public EffectType type;
    public double age = 0;
    public double colR;
    public double colG;
    public double colB;

    public boolean force = false;
    public boolean enableGlow = true;
    public double glowR;
    public double glowG;
    public double glowB;

    public double maxAge = 100;
    public double size;
    public double radius;
    public double angle;
    public double distance;

    //Effects that have this set to true are removed faster when the level has ended
    public boolean fastRemoveOnExit = false;

    public int drawLayer = 7;

    public State state = State.live;

    public static Effect createNewEffect(double x, double y, double z, EffectType type)
    {
        while (Game.recycleEffects.size() > 0)
        {
            Effect e = Game.recycleEffects.remove();

            if (e.state == State.recycle)
            {
                e.refurbish();
                e.initialize(x, y, z, type);
                return e;
            }
        }

        Effect e = new Effect();
        e.initialize(x, y, z, type);
        return e;
    }

    public static Effect createNewEffect(double x, double y, EffectType type, double age)
    {
        return Effect.createNewEffect(x, y, 0, type, age);
    }

    public static Effect createNewEffect(double x, double y, double z, EffectType type, double age)
    {
        Effect e = Effect.createNewEffect(x, y, z, type);
        e.age = age;
        return e;
    }

    public static Effect createNewEffect(double x, double y, EffectType type)
    {
        return Effect.createNewEffect(x, y, 0, type);
    }

    /**
     * Use Effect.createNewEffect(double x, double y, Effect.EffectType type) instead of this because it can refurbish and reuse old effects
     */
    protected Effect()
    {
        super(0, 0);
    }

    protected void initialize(double x, double y, double z, EffectType type)
    {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.type = type;

        if (type == EffectType.fire)
            this.maxAge = 20;
        else if (type == EffectType.smokeTrail)
            this.maxAge = 200;
        else if (type == EffectType.trail)
            this.maxAge = 50;
        else if (type == EffectType.ray)
            this.maxAge = 20;
        else if (type == EffectType.mineExplosion)
        {
            this.maxAge = 20;
            this.force = true;
        }
        else if (type == EffectType.laser)
            this.maxAge = 21;
        else if (type == EffectType.piece)
            this.maxAge = Math.random() * 100 + 50;
        else if (type == EffectType.obstaclePiece)
            this.maxAge = Math.random() * 100 + 50;
        else if (type == EffectType.obstaclePiece3d)
        {
            this.maxAge = Math.random() * 100 + 50;
            this.force = true;
        }
        else if (type.equals(EffectType.charge))
        {
            if (Game.enable3d)
                this.add3dPolarMotion(Math.random() * Math.PI * 2,-Math.random() * Math.PI / 2, Math.random() * 3 + 3);
            else
                this.addPolarMotion(Math.random() * Math.PI * 2, Math.random() * 3 + 3);

            this.posX -= this.vX * 25;
            this.posY -= this.vY * 25;
            this.posZ -= this.vZ * 25;
            this.maxAge = 25;
        }
        else if (type == EffectType.tread)
        {
            this.maxAge = 510;
            if (Game.effectsEnabled)
                this.maxAge *= 2;
        }
        else if (type == EffectType.darkFire)
            this.maxAge = 20;
        else if (type == EffectType.stun)
        {
            this.angle += Math.PI * 2 * Math.random();
            this.maxAge = 80 + Math.random() * 40;
            this.size = Math.random() * 5 + 5;
            this.distance = Math.random() * 50 + 25;
        }
        else if (type == EffectType.healing)
            this.maxAge = 21;
        else if (type == EffectType.bushBurn)
            this.maxAge = this.posZ * 2;
        else if (type == EffectType.glow)
            this.maxAge = 100;
        else if (type == EffectType.teleporterLight)
            this.maxAge = 0;
        else if (type == EffectType.teleporterPiece)
            this.maxAge = Math.random() * 100 + 50;
        else if (type == EffectType.interfacePiece)
        {
            this.maxAge = Math.random() * 100 + 50;
            this.force = true;
        }
        else if (type == EffectType.snow)
        {
            this.maxAge = Math.random() * 100 + 50;
            this.size = (Math.random() * 4 + 2) * Bullet.bullet_size;
        }
        else if (type == EffectType.shield)
            this.maxAge = 50;
        else if (type == EffectType.boostLight)
            this.maxAge = 0;
        else if (type == EffectType.exclamation)
            this.maxAge = 50;
    }

    protected void refurbish()
    {
        this.posX = 0;
        this.posY = 0;
        this.posZ = 0;
        this.vX = 0;
        this.vY = 0;
        this.vZ = 0;
        this.type = null;
        this.age = 0;
        this.colR = 0;
        this.colG = 0;
        this.colB = 0;
        this.glowR = 0;
        this.glowG = 0;
        this.glowB = 0;
        this.maxAge = Math.random() * 100 + 50;
        this.size = 0;
        this.angle = 0;
        this.distance = 0;
        this.radius = 0;
        this.enableGlow = true;
        this.drawLayer = 7;
        this.state = State.live;
        this.force = false;
        this.fastRemoveOnExit = false;
    }

    @Override
    public void draw()
    {
        if (this.maxAge > 0 && this.maxAge < this.age)
            return;

        if (this.type == EffectType.ray)
        {
            this.state = State.removed;
            Game.removeEffects.add(this);
        }

        if (!this.force && Game.sampleObstacleHeight(this.posX, this.posY) > this.posZ)
            return;

        if (this.age < 0)
            Game.exitToCrash(new RuntimeException("Effect with negative age"));

        double opacityMultiplier = ScreenGame.finishTimer / ScreenGame.finishTimerMax;
        Drawing drawing = Drawing.drawing;

        if (this.type == EffectType.fire)
        {
            double size = (this.age * 3 + 10);
            double rawOpacity = (1.0 - (this.age)/20.0);
            rawOpacity *= rawOpacity * rawOpacity;
            double opacity = (rawOpacity * 255) / 4;

            double green = Math.min(255, (255 - 255.0*(this.age / 20.0)));
            drawing.setColor(255, green, 0,  Math.min(255, Math.max(0, (opacity * opacityMultiplier))));

            if (Game.enable3d)
                drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
            else
                drawing.fillOval(this.posX, this.posY, size, size);
        }
        else if (this.type == EffectType.smokeTrail)
        {
            double opacityModifier = Math.max(0, Math.min(1, this.age / 40.0 - 0.25));
            int size = 20;
            double rawOpacity = (1.0 - (this.age)/200.0);
            rawOpacity *= rawOpacity * rawOpacity;
            double opacity = (rawOpacity * 100) / 2;

            drawing.setColor(0, 0, 0, Math.min(255, Math.max(0, (opacity * opacityMultiplier * opacityModifier))));

            if (Game.enable3d)
                drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
            else
                drawing.fillOval(this.posX, this.posY, size, size);
        }
        else if (this.type == EffectType.trail)
        {
            double size = Math.min(20, this.age / 20.0 + 10);
            double rawOpacity = (1.0 - (this.age) / 50.0);
            rawOpacity *= rawOpacity * rawOpacity;
            double opacity = (rawOpacity * 50);
            drawing.setColor(127, 127, 127, Math.min(255, Math.max(0, (opacity * opacityMultiplier))));

            if (Game.enable3d)
                drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
            else
                drawing.fillOval(this.posX, this.posY, size, size);
        }
        else if (this.type == EffectType.ray)
        {
            int size = 6;

            if (Level.isDark())
                Drawing.drawing.setColor(255, 255, 255, 50);
            else
                Drawing.drawing.setColor(0, 0, 0, 50);

            if (Game.enable3d)
                drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
            else
                drawing.fillOval(this.posX, this.posY, size, size);
        }
        else if (this.type == EffectType.mineExplosion)
        {
            double size = (radius * 2);
            double opacity = (100 - this.age * 5);
            drawing.setColor(255, 0, 0, opacity, 1);
            drawing.fillForcedOval(this.posX, this.posY, size, size);
        }
        else if (this.type == EffectType.laser)
        {
            double size = Bullet.bullet_size - this.age / 2;
            drawing.setColor(255, 0, 0);

            if (Game.enable3d)
                drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
            else
                drawing.fillOval(this.posX, this.posY, size, size);
        }
        else if (this.type == EffectType.piece)
        {
            double size = 1 + (Bullet.bullet_size * (1 - this.age / this.maxAge));
            drawing.setColor(this.colR, this.colG, this.colB, 255, 0.5);

            if (Game.enable3d)
                drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
            else
                drawing.fillOval(this.posX, this.posY, size, size);
        }
        else if (this.type == EffectType.snow)
        {
            double size2 = 1 + 1.5 * (Bullet.bullet_size * (1 - this.age / this.maxAge));
            drawing.setColor(this.colR, this.colG, this.colB);

            if (Game.enable3d)
                drawing.fillOval(this.posX, this.posY, this.posZ, size2, size2);
            else
                drawing.fillOval(this.posX, this.posY, size2, size2);
        }
        else if (this.type == EffectType.interfacePiece)
        {
            double size = 1 + (Bullet.bullet_size * (1 - this.age / this.maxAge));

            if (this.size > 0)
                size *= this.size;

            drawing.setColor(this.colR, this.colG, this.colB, 255, 0.5);
            drawing.fillInterfaceOval(this.posX, this.posY, size, size);
        }
        else if (this.type == EffectType.obstaclePiece)
        {
            double size = 1 + (Bullet.bullet_size * (1 - this.age / this.maxAge));
            drawing.setColor(this.colR, this.colG, this.colB);

            drawing.fillRect(this.posX, this.posY, size, size);
        }
        else if (this.type == EffectType.obstaclePiece3d)
        {
            double size = 1 + (12.5 * (1 - this.age / this.maxAge));
            drawing.setColor(this.colR, this.colG, this.colB);

            drawing.fillBox(this.posX, this.posY, this.posZ, size, size, size);
        }
        else if (this.type == EffectType.charge)
        {
            double size = 1 + (Bullet.bullet_size * (this.age / this.maxAge));
            drawing.setColor(this.colR, this.colG, this.colB, 255, 0.5);

            if (Game.enable3d)
                drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
            else
                drawing.fillOval(this.posX, this.posY, size, size);
        }
        else if (this.type == EffectType.tread)
        {
            double opacityFactor = 2;

            if (Game.effectsEnabled)
            {
                opacityFactor = 4;
            }

            double opacity = (255 - this.age / opacityFactor) / 4;
            drawing.setColor(0, 0, 0, opacity);
            drawing.drawModel(Drawing.rotatedRect, this.posX, this.posY, this.posZ, size * Obstacle.draw_size / Game.tile_size, size * Obstacle.draw_size / Game.tile_size, 1, angle, 0, 0);
        }
        else if (this.type == EffectType.darkFire)
        {
            double size = (this.age * 3 + 10);
            double rawOpacity = (1.0 - (this.age)/20.0);
            rawOpacity *= rawOpacity * rawOpacity;
            double opacity = (rawOpacity * 255) / 4;

            double red = Math.min(255, (128 - 128.0 * (this.age / 20.0)));
            drawing.setColor(red / 2, 0, red,  Math.min(255, Math.max(0, (opacity * opacityMultiplier))));

            if (Game.enable3d)
                drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
            else
                drawing.fillOval(this.posX, this.posY, size, size);
        }
        else if (this.type == EffectType.stun)
        {
            double size = 1 + (this.size * Math.min(Math.min(1, (this.maxAge - this.age) * 3 / this.maxAge), Math.min(1, this.age * 3 / this.maxAge)));
            double angle = this.angle + this.age / 20;
            double distance = 1 + (this.distance * Math.min(Math.min(1, (this.maxAge - this.age) * 3 / this.maxAge), Math.min(1, this.age * 3 / this.maxAge)));

            drawing.setColor(this.colR, this.colG, this.colB, 255, 0.5);
            double[] o = Movable.getLocationInDirection(angle, distance);

            if (Game.enable3d)
                drawing.fillOval(this.posX + o[0], this.posY + o[1], this.posZ, size, size);
            else
                drawing.fillOval(this.posX + o[0], this.posY + o[1], size, size);
        }
        else if (this.type == EffectType.electric)
        {
            double size = Math.max(0, Bullet.bullet_size - this.age / 2);
            drawing.setColor(0, 255, 255);

            if (Game.enable3d)
                drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
            else
                drawing.fillOval(this.posX, this.posY, size, size);
        }
        else if (this.type == EffectType.healing)
        {
            double size = Bullet.bullet_size - this.age / 2;
            drawing.setColor(0, 255, 0);

            if (Game.enable3d)
                drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
            else
                drawing.fillOval(this.posX, this.posY, size, size);
        }
        else if (this.type == EffectType.bushBurn)
        {
            if (Game.enable3d)
            {
                Drawing.drawing.setColor(this.colR, this.colG, this.colB);
                Drawing.drawing.fillBox(this.posX, this.posY, 0, Obstacle.draw_size, Obstacle.draw_size, this.posZ);
            }
            else
            {
                Drawing.drawing.setColor(this.colR, this.colG, this.colB, this.posZ);
                Drawing.drawing.fillRect(this.posX, this.posY, Obstacle.draw_size, Obstacle.draw_size);
            }

            if (!Game.game.window.drawingShadow)
                this.posZ -= Panel.frameFrequency / 2;

            this.colR = Math.max(this.colR - Panel.frameFrequency, 0);
            this.colG = Math.max(this.colG - Panel.frameFrequency, 0);
            this.colB = Math.max(this.colB - Panel.frameFrequency, 0);
        }
        else if (this.type == EffectType.glow)
        {
            double size = 1 + (40 * (1 - this.age / this.maxAge));
            drawing.setColor(255, 255, 255, 40);

            if (Game.enable3d)
            {
                drawing.fillOval(this.posX, this.posY, this.posZ, size, size, false, true);
                drawing.fillOval(this.posX, this.posY, this.posZ, size / 2, size / 2, false, true);
            }
            else
            {
                drawing.fillOval(this.posX, this.posY, size, size);
                drawing.fillOval(this.posX, this.posY, size / 2, size / 2);
            }
        }
        else if (this.type == EffectType.teleporterLight)
        {
            for (double i = 0; i < 1 - this.size; i += 0.025)
            {
                Drawing.drawing.setColor(255, 255, 255, (1 - this.size - i) * 25, 1);
                Drawing.drawing.fillOval(this.posX, this.posY, posZ + 7 + i * 50, Obstacle.draw_size / 2, Obstacle.draw_size / 2, true, false);
            }
        }
        else if (this.type == EffectType.teleporterPiece)
        {
            double size = 1 + (Bullet.bullet_size * (1 - this.age / this.maxAge));
            drawing.setColor(this.colR, this.colG, this.colB, 255, 0.5);

            if (Game.enable3d)
                drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
            else
                drawing.fillOval(this.posX, this.posY, size, size);
        }
        else if (this.type == EffectType.shield)
        {
            double a = Math.min(25, 50 - this.age) * 2.55 * 4;
            drawing.setColor(255, 255, 255, a);

            if (Game.enable3d)
            {
                drawing.drawImage("shield.png", this.posX, this.posY, this.posZ + this.age, this.size * 1.25, this.size * 1.25);
                drawing.setFontSize(24 * this.size / Game.tile_size);
                drawing.setColor(0, 0, 0, a, 0.5);
                drawing.drawText(this.posX, this.posY - this.size / 20, this.posZ + this.age + 1, "" + (int) this.radius);
            }
            else
            {
                drawing.drawImage("shield.png", this.posX, this.posY, this.size * 1.25, this.size * 1.25);
                drawing.setFontSize(24 * this.size / Game.tile_size);
                drawing.setColor(0, 0, 0, a, 0.5);
                drawing.drawText(this.posX, this.posY - this.size / 20, "" + (int) this.radius);
            }
        }
        else if (this.type == EffectType.boostLight)
        {
            Drawing.drawing.setColor(255, 255, 255, 255, 1);
            Game.game.window.shapeRenderer.setBatchMode(true, true, true, true, false);

            double max = (this.size);
            for (int i = 0; i < max; i++)
            {
                double a = (max - i) / 400;
                Drawing.drawing.setColor(255 * a, 255 * a, 200 * a, 255, 1.0);
                Drawing.drawing.fillBox(this.posX, this.posY, i, Obstacle.draw_size, Obstacle.draw_size, 0, (byte) 62);
            }

            Game.game.window.shapeRenderer.setBatchMode(false, true, true, true, false);
        }
        else if (this.type == EffectType.exclamation)
        {
            double a = Math.min(25, 50 - this.age) * 2.55 * 4;
            drawing.setColor(255, 100, 255, a, 0.5);

            if (Game.enable3d)
            {
                drawing.fillOval(this.posX, this.posY, this.posZ + this.age, this.size, this.size);
                drawing.setColor(255, 200, 255, a, 0);
                drawing.fillOval(this.posX, this.posY, this.posZ + this.age, this.size * 0.8, this.size * 0.8);
                drawing.setFontSize(32 * this.size / Game.tile_size);
                drawing.setColor(255, 0, 0, a, 1);
                drawing.drawText(this.posX + 2, 3 + this.posY - this.size / 20, this.posZ + this.age + 1, "!");
            }
            else
            {
                drawing.fillOval(this.posX, this.posY,this.size, this.size);
                drawing.setColor(255, 200, 255, a, 0);
                drawing.fillOval(this.posX, this.posY, this.posZ + this.age, this.size * 0.8, this.size * 0.8);
                drawing.setFontSize(32 * this.size / Game.tile_size);
                drawing.setColor(255, 0, 0, a, 1);
                drawing.drawText(this.posX + 2, 3 + this.posY - this.size / 20, "!");
            }
        }
        else
        {
            Game.exitToCrash(new RuntimeException("Invalid effect type!"));
        }
    }

    public void drawGlow()
    {
        if (this.maxAge > 0 && this.maxAge < this.age)
            return;

        if (!this.force && Game.sampleObstacleHeight(this.posX, this.posY) > this.posZ)
            return;

        if (this.age < 0)
            Game.exitToCrash(new RuntimeException("Effect with negative age"));

        Drawing drawing = Drawing.drawing;

        if (this.type == EffectType.piece)
        {
            double size = 1 + (Bullet.bullet_size * (1 - this.age / this.maxAge));

            drawing.setColor(this.colR - this.glowR, this.colG - this.glowG, this.colB - this.glowB, 127, 1);

            if (Game.enable3d)
                drawing.fillGlow(this.posX, this.posY, this.posZ, size * 8, size * 8);
            else
                drawing.fillGlow(this.posX, this.posY, size * 8, size * 8);
        }
        if (this.type == EffectType.interfacePiece)
        {
            double size = 1 + (Bullet.bullet_size * (1 - this.age / this.maxAge));

            if (this.size > 0)
                size *= this.size;

            drawing.setColor(this.colR - this.glowR, this.colG - this.glowG, this.colB - this.glowB, 127, 1);

            drawing.fillInterfaceGlow(this.posX, this.posY, size * 8, size * 8);
        }
        else if (this.type == EffectType.charge)
        {
            double size = 1 + (Bullet.bullet_size * (this.age / this.maxAge));

            drawing.setColor(this.colR - this.glowR, this.colG - this.glowG, this.colB - this.glowB, 127, 1);

            if (Game.enable3d)
                drawing.fillGlow(this.posX, this.posY, this.posZ, size * 8, size * 8);
            else
                drawing.fillGlow(this.posX, this.posY, size * 8, size * 8);
        }
        else if (this.type == EffectType.stun)
        {
            double size = 1 + (this.size * Math.min(Math.min(1, (this.maxAge - this.age) * 3 / this.maxAge), Math.min(1, this.age * 3 / this.maxAge)));
            double angle = this.angle + this.age / 20;
            double distance = 1 + (this.distance * Math.min(Math.min(1, (this.maxAge - this.age) * 3 / this.maxAge), Math.min(1, this.age * 3 / this.maxAge)));

            double[] o = Movable.getLocationInDirection(angle, distance);

            drawing.setColor(this.colR - this.glowR, this.colG - this.glowG, this.colB - this.glowB, 255, 1);

            if (Game.enable3d)
                drawing.fillGlow(this.posX + o[0], this.posY + o[1], this.posZ, size * 8, size * 8);
            else
                drawing.fillGlow(this.posX + o[0], this.posY + o[1], size * 8, size * 8);
        }
        else if (this.type == EffectType.glow)
        {
            double size = 1 + (40 * (1 - this.age / this.maxAge));

            drawing.setColor(255, 255, 255, 40, 1);

            if (Game.enable3d)
                drawing.fillGlow(this.posX, this.posY, this.posZ, size * 8, size * 8, false, true);
            else
                drawing.fillGlow(this.posX, this.posY, size * 8, size * 8);
        }
        else if (this.type == EffectType.teleporterPiece)
        {
            double size = 1 + (Bullet.bullet_size * (1 - this.age / this.maxAge));

            drawing.setColor(this.colR - this.glowR, this.colG - this.glowG, this.colB - this.glowB, 127, 1);

            if (Game.enable3d)
                drawing.fillGlow(this.posX, this.posY, this.posZ, size * 8, size * 8, false, true);
            else
                drawing.fillGlow(this.posX, this.posY, size * 8, size * 8);
        }
        else if (this.type == EffectType.snow)
        {
            double size = this.size * (1 + this.age / this.maxAge);
            drawing.setColor(this.colR, this.colG, this.colB, (1 - this.age / this.maxAge) * 255);

            if (Game.enable3d)
                drawing.fillGlow(this.posX, this.posY, this.posZ, size, size, true);
            else
                drawing.fillGlow(this.posX, this.posY, size, size, true);

            /*if (Game.enable3d)
                drawing.drawImage("glow.png", this.posX, this.posY, this.posZ, size, size);
            else
                drawing.drawImage("glow.png", this.posX, this.posY, size, size);*/
        }
        else if (this.type == EffectType.ray)
        {
            drawing.setColor(255, 255, 255, 50, 1);

            if (Game.enable3d)
                drawing.fillGlow(this.posX, this.posY, this.posZ, 24, 24, false);
            else
                drawing.fillGlow(this.posX, this.posY, 24, 24, false);
        }
    }

    @Override
    public boolean isGlowEnabled()
    {
        return this.enableGlow;
    }

    @Override
    public void update()
    {
        this.posX += this.vX * Panel.frameFrequency;
        this.posY += this.vY * Panel.frameFrequency;
        this.posZ += this.vZ * Panel.frameFrequency;

        if (this.maxAge >= 0)
            this.age += Panel.frameFrequency;

        if (this.fastRemoveOnExit && ScreenGame.finishedQuick)
            this.age += Panel.frameFrequency * 4;

        if (this.maxAge > 0 && this.age > this.maxAge && this.state == State.live)
        {
            this.state = State.removed;

            if (Game.effects.contains(this) && !Game.removeEffects.contains(this))
                Game.removeEffects.add(this);
            else if (Game.tracks.contains(this) && !Game.removeTracks.contains(this))
                Game.removeTracks.add(this);
        }
    }
}
