package net.vas.rdpcore.region;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Represents an RDP hotspot with position, radius and intensity.
 */
public class Hotspot implements INBTSerializable<NBTTagCompound> {

    public int x, y, z;
    public double radius;
    public double intensity;
    public int age;
    public String type;

    public Hotspot() {}

    public Hotspot(int x, int y, int z, double radius, double intensity, String type) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.intensity = intensity;
        this.type = type;
        this.age = 0;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("x", x);
        tag.setInteger("y", y);
        tag.setInteger("z", z);
        tag.setDouble("radius", radius);
        tag.setDouble("intensity", intensity);
        tag.setInteger("age", age);
        tag.setString("type", type == null ? "generic" : type);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.x = nbt.getInteger("x");
        this.y = nbt.getInteger("y");
        this.z = nbt.getInteger("z");
        this.radius = nbt.getDouble("radius");
        this.intensity = nbt.getDouble("intensity");
        this.age = nbt.getInteger("age");
        this.type = nbt.getString("type");
    }
}
