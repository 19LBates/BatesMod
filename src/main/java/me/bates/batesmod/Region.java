package me.bates.batesmod;

public class Region {
    private final String name;
    private final int lowerX;
    private final int lowerY;
    private final int lowerZ;
    private final int upperX;
    private final int upperY;
    private final int upperZ;

    public Region(String name, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.name = name;
        if (x1 < x2) {
            this.lowerX = x1;
            this.upperX = x2;
        } else {
            this.lowerX = x2;
            this.upperX = x1;
        }

        if (y1 < y2) {
            this.lowerY = y1;
            this.upperY = y2;
        } else {
            this.lowerY = y2;
            this.upperY = y1;
        }

        if (z1 < z2) {
            this.lowerZ = z1;
            this.upperZ = z2;
        } else {
            this.lowerZ = z2;
            this.upperZ = z1;
        }
    }

    public boolean isInRegion(int x, int y, int z) {
        return (x >= lowerX && x <= upperX) && (y >= lowerY && y <= upperY) && (z >= lowerZ && z <= upperZ);
    }

    public String getName() {
        return this.name;
    }

   //public int getLowerX() {
   //    return lowerX;
   //}

   //public int getLowerY() {
   //    return lowerY;
   //}

   //public int getLowerZ() {
   //    return lowerZ;
   //}

   //public int getUpperX() {
   //    return upperX;
   //}

   //public int getUpperY() {
   //    return upperY;
   //}

   //public int getUpperZ() {
   //    return upperZ;
   //}
}
