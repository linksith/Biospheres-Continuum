package woop;

import java.util.Random;

import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.biome.BiomeGenBase;

public class SphereChunk
{
	public final int chunkX, chunkZ;

	public final BiosphereChunkProvider chunkProvider;
	public final ChunkCoordinates sphereLocation;
	public final ChunkCoordinates orbLocation;
	public final ChunkCoordinates lakeLocation;

	private final long seed;

	public final double radius;

	public final double lakeRadius;
	public final double lakeEdgeRadius;

	public final boolean lavaLake;
	public final boolean hasLake;

	public final BiomeGenBase biome;

	public SphereChunk(BiosphereChunkProvider chunkProvider, int chunkX, int chunkZ)
	{
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;

		this.chunkProvider = chunkProvider;
		ModConfig cfg = this.chunkProvider.config;

		// Set sphere location
		this.sphereLocation = GetSphereCenter(chunkX, chunkZ);

		// Seed local random number generator
		Random rnd = new Random(chunkProvider.worldSeed);
		long xm = rnd.nextLong() / 2L * 2L + 1L;
		long zm = rnd.nextLong() / 2L * 2L + 1L;
		long _seed = ((long)sphereLocation.posX * xm + (long)sphereLocation.posZ * zm) * 2512576L ^ chunkProvider.worldSeed;
		rnd.setSeed(_seed);

		double minRad = cfg.getMinSphereRadius() * cfg.getScale();
		double maxRad = cfg.getMaxSphereRadius() * cfg.getScale();

		double radRange = (maxRad - minRad);

		// Get sphere radius
		this.radius = Math.round(minRad + (rnd.nextDouble() * radRange));

		// Get lake radius
		double lakeRatio = cfg.getMinLakeRatio()
				+ ((cfg.getMaxLakeRatio() - cfg.getMinLakeRatio()) * rnd.nextDouble());
		this.lakeRadius = (double)Math.round(this.radius * lakeRatio);
		this.lakeEdgeRadius = lakeRadius + 2.0d;

		this.biome = this.chunkProvider.world.getWorldChunkManager().getBiomeGenAt(sphereLocation.posX, sphereLocation.posZ);

		this.lavaLake = this.biome == BiomeGenBase.hell || this.biome != BiomeGenBase.swampland
				&& this.biome != BiomeGenBase.taiga && this.biome != BiomeGenBase.icePlains
				&& this.biome != BiomeGenBase.sky && rnd.nextInt(10) == 0;
		this.hasLake = this.biome == BiomeGenBase.swampland || this.biome != BiomeGenBase.sky
				&& rnd.nextInt(2) == 0;

		orbLocation = new ChunkCoordinates();
		orbLocation.posY = cfg.getScaledOrbRadius() + 1
				+ rnd.nextInt(ModConsts.WORLD_MAX_Y - (cfg.getScaledOrbRadius() + 1));
		orbLocation.posX = this.sphereLocation.posX + cfg.getScaledGridSize() / 2 * 16 - cfg.getScaledOrbRadius();
		orbLocation.posZ = this.sphereLocation.posZ + cfg.getScaledGridSize() / 2 * 16 - cfg.getScaledOrbRadius();

		lakeLocation = new ChunkCoordinates();
		lakeLocation.posX = sphereLocation.posX;
		lakeLocation.posY = sphereLocation.posY;
		lakeLocation.posZ = sphereLocation.posZ;

		// if (NOISE)
		// {
		// this.setNoise(this.midX >> 4, this.midZ >> 4);
		// this.noiseMin = Double.MAX_VALUE;
		//
		// for (int k = 0; k < this.noise.length; ++k)
		// {
		// if (this.noise[k] < this.noiseMin)
		// {
		// this.noiseMin = this.noise[k];
		// }
		// }
		//
		// lake.posY = (int)Math.round(seaLevel + this.noiseMin * 8.0D * 1.0D);
		// this.setNoise(chunkX, chunkZ);
		// }

		// Reseed random generator
		xm = rnd.nextLong() / 2L * 2L + 1L;
		zm = rnd.nextLong() / 2L * 2L + 1L;
		this.seed = ((long)chunkX * xm + (long)chunkZ * zm) * 3168045L ^ this.chunkProvider.worldSeed;
	}

	public Random GetPhaseRandom(String phase)
	{
		Random rnd = new Random(this.seed);

		long xm = rnd.nextLong() / 2L * 2L + 1L;
		long zm = rnd.nextLong() / 2L * 2L + 1L;

		long _seed = ((long)chunkX * xm + (long)chunkZ * zm) * (long)phase.hashCode() ^ this.chunkProvider.worldSeed;

		rnd.setSeed(_seed);
		return rnd;
	}

	private ChunkCoordinates GetSphereCenter(int chunkX, int chunkZ)
	{
		ModConfig cfg = this.chunkProvider.config;
		
		int chunkOffsetToCenterX = -(int)Math.floor(Math.IEEEremainder(
			(double)chunkX,
			(double)cfg.getScaledGridSize()));
		
		int chunkOffsetToCenterZ = -(int)Math.floor(Math.IEEEremainder(
			(double)chunkZ,
			(double)cfg.getScaledGridSize()));

		ChunkCoordinates cc = new ChunkCoordinates();

		cc.posX = ((chunkX + chunkOffsetToCenterX) << 4) + 8;
		cc.posY = ModConsts.SEA_LEVEL; // getSurfaceLevel(8, 8);
		cc.posZ = ((chunkZ + chunkOffsetToCenterZ) << 4) + 8;

		return cc;
	}

	public int getMainDistance(int rawX, int rawY, int rawZ)
	{
		return Utils.GetDistance(this.sphereLocation, rawX, rawY, rawZ);
	}

	public int getOrbDistance(int rawX, int rawY, int rawZ)
	{
		return Utils.GetDistance(this.orbLocation, rawX, rawY, rawZ);
	}

	public int getSurfaceLevel(int x, int z)
	{
		return ModConsts.SEA_LEVEL;
		// return NOISE ? (int)Math.round(seaLevel + this.noise[z + (x * 16)] * 8.0D * scale) : seaLevel;
	}
}