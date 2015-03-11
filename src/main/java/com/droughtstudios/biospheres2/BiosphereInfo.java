package com.droughtstudios.biospheres2;

import java.awt.Point;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
import net.minecraft.util.Vec3;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraftforge.fml.common.FMLLog;

/**
 * Created by Trevor on 2/11/2015.
 */
public class BiosphereInfo {

	// biospheres takes SIZExSIZE chunks
	public static final int BIOSPHERE_CHUNK_SIZE = 16;
	public static final int BIOSPHERE_RADIUS_CHUNKS = 5;
	public static final int BIOSPHERE_MAX_HEIGHT = 128;
	public static final int BIOSPHERE_MIN_HEIGHT = BIOSPHERE_RADIUS_CHUNKS * BIOSPHERE_CHUNK_SIZE;
    public static final int BIOSPHERE_ORE_THING_RADIUS_CHUNKS = 1;

	public static boolean DOME_ENABLED = true;

	private static WeightedMap<WeightedMap<BiomeGenBase>> biomeTypes;

    public Vec3 oreThingCenter;
    public int oreThingRadius;
	public Vec3 worldCenter;
	public int radius;
	public BiomeGenBase biome;

	public Point featurePosition = null;
	public Class<? extends MapGenStructure> featureType = null;

	public BiosphereInfo(Point biosphereLocation, Random random) {
		// set biome ===================================================================================================
		genWeightedBiomeList();

		biome = biomeTypes.getRandom(random).getRandom(random);

		// set world location ==========================================================================================

		radius = BIOSPHERE_RADIUS_CHUNKS * 16;

		// spawn area is subsquare of total area; subsquare is -radius on each side
		int spawnAreaSize = (BIOSPHERE_CHUNK_SIZE * 16) - 2 * radius;
		float spawnX = radius + random.nextInt(spawnAreaSize);
		float spawnY = radius + random.nextInt(spawnAreaSize);

		double biomeMinHeight = 64.0 + 32.0 * biome.minHeight;
		double biomeMaxHeight = 64.0 + 32.0 * (biome.minHeight + biome.maxHeight);
		double biomeHeight = Math.max(BIOSPHERE_MIN_HEIGHT,
		                              Math.min(BIOSPHERE_MAX_HEIGHT,
		                                       biomeMinHeight + ((biomeMaxHeight - biomeMinHeight) / 2.0)));

		worldCenter = new Vec3(biosphereLocation.x * BIOSPHERE_CHUNK_SIZE * 16.0 + spawnX,
		                       biomeHeight,
		                       biosphereLocation.y * BIOSPHERE_CHUNK_SIZE * 16.0 + spawnY);

        // make our little ore buddy ===================================================================================

        doinOTMaths(biosphereLocation, random, biomeHeight);
        FMLLog.info("======================================================");
        FMLLog.info("orething center: " + oreThingCenter);
        FMLLog.info("biosphere center: " + worldCenter);
        String omg = "ok ok ok ";
        if(inRadius((int)oreThingCenter.xCoord,(int)oreThingCenter.yCoord,(int)oreThingCenter.zCoord)){omg = "OMG";}
        FMLLog.info(omg);
	}


	public boolean inRadius(int worldPosX, int worldPosY, int worldPosZ) {
		double dx = worldPosX - worldCenter.xCoord;
		double dy = worldPosY - worldCenter.yCoord;
		double dz = worldPosZ - worldCenter.zCoord;
		return ((dx * dx) + (dy * dy) + (dz * dz)) < radius * radius;
	}

	public boolean inRadius(int worldPosX, int worldPosY) {
		double dx = worldPosX - worldCenter.xCoord;
		double dz = worldPosY - worldCenter.zCoord;
		return ((dx * dx) + (dz * dz)) < radius * radius;
	}

	public boolean inInnerRadius(int worldPosX, int worldPosY) {
		double dx = worldPosX - worldCenter.xCoord;
		double dz = worldPosY - worldCenter.zCoord;
		return ((dx * dx) + (dz * dz)) < radius;
	}

	private void genWeightedBiomeList() {
		if (biomeTypes != null) return;

		// todo don't use biomes directly, make more extendable with other mods
		WeightedMap<BiomeGenBase> ocean = new WeightedMap<BiomeGenBase>()
				.add(BiomeGenBase.ocean, 0.4)
				.add(BiomeGenBase.deepOcean, 0.4)
				.add(BiomeGenBase.frozenOcean, 0.2);

		WeightedMap<BiomeGenBase> plains = new WeightedMap<BiomeGenBase>()
				.add(BiomeGenBase.plains, 0.5)
				.add(BiomeGenBase.savanna, 0.3)
				.add(BiomeGenBase.savannaPlateau, 0.2);

		WeightedMap<BiomeGenBase> sandy = new WeightedMap<BiomeGenBase>()
				.add(BiomeGenBase.desert, 0.3)
				.add(BiomeGenBase.desertHills, 0.2)
				.add(BiomeGenBase.mesa, 0.25)
				.add(BiomeGenBase.mesaPlateau, 0.15)
				.add(BiomeGenBase.mesaPlateau_F, 0.1);

		WeightedMap<BiomeGenBase> mountain = new WeightedMap<BiomeGenBase>()
				.add(BiomeGenBase.extremeHills, 0.4)
				.add(BiomeGenBase.iceMountains, 0.1)
				.add(BiomeGenBase.extremeHillsEdge, 0.2)
				.add(BiomeGenBase.extremeHillsPlus, 0.3);

		WeightedMap<BiomeGenBase> forest = new WeightedMap<BiomeGenBase>()
				.add(BiomeGenBase.forest, 0.3)
				.add(BiomeGenBase.forestHills, 0.2)
				.add(BiomeGenBase.birchForest, 0.2)
				.add(BiomeGenBase.birchForestHills, 0.1)
				.add(BiomeGenBase.roofedForest, 0.2);

		WeightedMap<BiomeGenBase> coniferous = new WeightedMap<BiomeGenBase>()
				.add(BiomeGenBase.taiga, 0.2)
				.add(BiomeGenBase.taigaHills, 0.2)
				.add(BiomeGenBase.coldTaiga, 0.2)
				.add(BiomeGenBase.coldTaigaHills, 0.1)
				.add(BiomeGenBase.megaTaiga, 0.2)
				.add(BiomeGenBase.megaTaigaHills, 0.1);

		WeightedMap<BiomeGenBase> wasteland = new WeightedMap<BiomeGenBase>()
				.add(BiomeGenBase.icePlains, 1);

		WeightedMap<BiomeGenBase> beach = new WeightedMap<BiomeGenBase>()
				.add(BiomeGenBase.beach, 0.6)
				.add(BiomeGenBase.stoneBeach, 0.3)
				.add(BiomeGenBase.coldBeach, 0.1);

		WeightedMap<BiomeGenBase> wet = new WeightedMap<BiomeGenBase>()
				.add(BiomeGenBase.swampland, 0.4)
				.add(BiomeGenBase.jungle, 0.3)
				.add(BiomeGenBase.jungleHills, 0.2)
				.add(BiomeGenBase.jungleEdge, 0.1);

		WeightedMap<BiomeGenBase> mushroom = new WeightedMap<BiomeGenBase>()
				.add(BiomeGenBase.mushroomIsland, 0.8)
				.add(BiomeGenBase.mushroomIslandShore, 0.2);

		biomeTypes = new WeightedMap<WeightedMap<BiomeGenBase>>()
				.add(ocean, 0.1)
				.add(plains, 0.15)
				.add(sandy, 0.1)
				.add(mountain, 0.2)
				.add(forest, 0.1)
				.add(coniferous, 0.1)
				.add(wasteland, 0.05)
				.add(beach, 0.05)
				.add(wet, 0.1)
				.add(mushroom, 0.05);
	}

	private class WeightedMap<T> {
		private final NavigableMap<Double, T> mWeightedmap = new TreeMap<>();
		private double total = 0;

		public WeightedMap<T> add(T item, double weight) {
			mWeightedmap.put(total, item);
			total += weight;
			return this;
		}

		public T getRandom(Random random) {
			return mWeightedmap.floorEntry(random.nextDouble()).getValue();
		}
	}


    /************************************************************************************************
     * The math for generating our little ore bud resides here                                      *
     *                                                                                              *
     * We grab a random xz coordinate within the chunk area for the biosphere,                      *
     * then check if creating the ore thing sphere at that location COULD intersect                 *
     * with the biosphere at ANY height. If it wouldn't, we grab a random height.                   *
     * If it could, we check to see if there exists a valid height where it wouldn't intersect.     *
     * If there is, we generate a random valid height.                                              *
     * If not, we pick a new xz coordinate and do it all over again.                                *
     ************************************************************************************************/
    private void doinOTMaths(Point biosphereLocation, Random random, double sphereHeight){
        oreThingRadius = BIOSPHERE_ORE_THING_RADIUS_CHUNKS * 16;
        int spawnAreaSizeXZOT = (BIOSPHERE_CHUNK_SIZE * 16) - (oreThingRadius * 2);
        int spawnAreaSizeHeightOT = 256 - (oreThingRadius * 2);
        double oreCoordX = 0;
        double oreCoordY = 0;
        double oreCoordZ = 0;
        int bufferedSphereRadius = radius + 2;

        boolean nope = true;
        while (nope) {
            float potentialSpawnX = oreThingRadius + random.nextInt(spawnAreaSizeXZOT);
            float potentialSpawnZ = oreThingRadius + random.nextInt(spawnAreaSizeXZOT);

            //finding the relative difference between the Ore Thing and the Biosphere
            float radiusAllTogetherNow = oreThingRadius + bufferedSphereRadius;
            double OTDistanceXZSq = (Math.abs(worldCenter.xCoord-potentialSpawnX) * Math.abs(worldCenter.xCoord-potentialSpawnX))
                    +(Math.abs(worldCenter.zCoord-potentialSpawnZ) * Math.abs(worldCenter.zCoord-potentialSpawnZ));
            double sphereAtYSq = (bufferedSphereRadius * bufferedSphereRadius) - OTDistanceXZSq;

            //If the XZ difference is less than the two radiuses, we need to see if the height allows for some space to generate
            boolean sqrtNecessity = false;
            if(OTDistanceXZSq <= radiusAllTogetherNow*radiusAllTogetherNow){
                //is there no space above?
                if(sphereAtYSq < (256 - (oreThingRadius + sphereHeight)) * (256 - (oreThingRadius + sphereHeight))){
                    //is there no space below?
                    if (sphereAtYSq <= (sphereHeight - oreThingRadius) * (sphereHeight - oreThingRadius)){
                        //YOU HAVE NOTHING, NOTHING AT ALL TRY AGAIN
                    } else {sqrtNecessity = true;}
                } else {sqrtNecessity = true;}
            } else {
                //Everything is fine, no more calculating, move on
                nope = false;
            }
/**
 * orething center: (-384.0, 112.0, 318.0)
 [01:19:55] [Server thread/INFO] [FML]: biosphere center: (-348.0, 112.39999961853027, 386.0)
 [01:19:55] [Server thread/INFO] [FML]: OMG
 */
            //If we have space but COULD intersect the biosphere, we need to figure a safe height to be
            if (sqrtNecessity){
                //Absolute value height from sphere height in which the biosphere is currently being a sphere at this x,z
                // + oreThing buffer
                double disallowedY = Math.sqrt(sphereAtYSq) + oreThingRadius;

                //Make an array of allowed height spots
                int arrayLength = (int) (spawnAreaSizeHeightOT - (disallowedY * 2));
                double[] allowedHeight = new double[arrayLength];
                int index = 0;
                for (int potentialHeight = oreThingRadius; index < allowedHeight.length; potentialHeight++){
                    if(potentialHeight < (sphereHeight - disallowedY)){
                        allowedHeight[index] = potentialHeight;
                        index++;
                    }
                    //loop will do nothing for the values representing biosphereland
                    if (potentialHeight > (sphereHeight + disallowedY)){
                        allowedHeight[index] = potentialHeight;
                        index++;
                    }
                }

                //Find a random height within this array
                oreCoordY = (int) allowedHeight[random.nextInt(allowedHeight.length)];

                //set the rest
                oreCoordX = potentialSpawnX;
                oreCoordZ = potentialSpawnZ;
                //nope = !inRadius((int)oreCoordX,(int)oreCoordY,(int)oreCoordZ);
            //Set a simple random height if we're not intersecting anything and are about to move on emotionally
            } else if ( !nope ){
                oreCoordY = oreThingRadius + (random.nextInt(spawnAreaSizeHeightOT));

                //set the rest
                oreCoordX = potentialSpawnX;
                oreCoordZ = potentialSpawnZ;
                //nope = !inRadius((int)oreCoordX,(int)oreCoordY,(int)oreCoordZ);
            }
        }
        oreThingCenter = new Vec3((int)(biosphereLocation.x * BIOSPHERE_CHUNK_SIZE * 16 + oreCoordX),
                (int)oreCoordY,
                (int)(biosphereLocation.y * BIOSPHERE_CHUNK_SIZE * 16 + oreCoordZ));
    }
}
