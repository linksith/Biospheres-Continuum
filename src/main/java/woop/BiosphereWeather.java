package woop;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import net.minecraft.util.WeightedRandom;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.layer.IntCache;

public class BiosphereWeather extends WorldChunkManager
{
	public static final List biomeList;
	private long seed;
	private Random rnd;
	public final float scale;
	public final int scaledGrid;

	public BiosphereWeather()
	{
		this.rnd = new Random();
		this.seed = this.rnd.nextLong();
		this.scale = 1.0F;
		this.scaledGrid = BiosphereGen.GRID_SIZE;
	}

	public BiosphereWeather(World world1)
	{
		this(world1.getSeed(), WoopMod.Biosphere);
	}

	public BiosphereWeather(long par1, WorldType par3WorldType)
	{
		this.seed = par1;
		this.scale = 1.0F;
		this.scaledGrid = (int)((float)BiosphereGen.GRID_SIZE * this.scale);
		this.rnd = new Random(this.seed);
	}

	/**
	 * checks given Chunk's Biomes against List of allowed ones
	 */
	public boolean areBiomesViable(int i, int j, int k, List list)
	{
		return true;
	}

	/**
	 * Return an adjusted version of a given temperature based on the y height
	 */
	public float getTemperatureAtHeight(float f, int i)
	{
		return f;
	}

	public float getHumid(int i, int j)
	{
		float f = this.getBiomeGenAt(i, j).rainfall;
		return f <= 1.0F ? f : 1.0F;
	}

	/**
	 * Returns a list of rainfall values for the specified blocks. Args:
	 * listToReuse, x, z, width, length.
	 */
	public float[] getRainfall(float[] af, int i, int j, int k, int l)
	{
		IntCache.resetIntCache();

		if (af == null || af.length < k * l)
		{
			af = new float[k * l];
		}

		float f = this.getHumid(i, j);
		int i1 = 0;

		for (int j1 = 0; j1 < k; ++j1)
		{
			for (int k1 = 0; k1 < l; ++k1)
			{
				af[i1] = f;
				++i1;
			}
		}

		return af;
	}

	public float getTemp(int i, int j)
	{
		float f = this.getBiomeGenAt(i, j).temperature;
		return f <= 1.0F ? f : 1.0F;
	}

	/**
	 * Returns a list of temperatures to use for the specified blocks. Args:
	 * listToReuse, x, y, width, length
	 */
	public float[] getTemperatures(float[] af, int i, int j, int k, int l)
	{
		IntCache.resetIntCache();

		if (af == null || af.length < k * l)
		{
			af = new float[k * l];
		}

		float f = this.getTemp(i, j);
		int i1 = 0;

		for (int j1 = 0; j1 < k; ++j1)
		{
			for (int k1 = 0; k1 < l; ++k1)
			{
				af[i1] = f;
				++i1;
			}
		}

		return af;
	}

	/**
	 * Returns the BiomeGenBase related to the x, z position on the world.
	 */
	public BiomeGenBase getBiomeGenAt(int i, int j)
	{
		int k = i >> 4;
		int l = j >> 4;
		int i1 = (k - (int)Math.floor(Math.IEEEremainder((double)k, (double)this.scaledGrid)) << 4) + 8;
		int j1 = (l - (int)Math.floor(Math.IEEEremainder((double)l, (double)this.scaledGrid)) << 4) + 8;
		this.rnd.setSeed(this.seed);
		long l1 = this.rnd.nextLong() / 2L * 2L + 1L;
		long l2 = this.rnd.nextLong() / 2L * 2L + 1L;
		this.rnd.setSeed(((long)i1 * l1 + (long)j1 * l2) * 7215145L ^ this.seed);
		return ((BiomeEntry)WeightedRandom.getRandomItem(this.rnd, biomeList)).biome;
	}

	/**
	 * Returns an array of biomes for the location input.
	 */
	public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] abiomegenbase, int i, int j, int k, int l)
	{
		return this.getBiomeGenAt(abiomegenbase, i, j, k, l, false);
	}

	/**
	 * Return a list of biomes for the specified blocks. Args: listToReuse, x,
	 * y, width, length, cacheFlag (if false, don't check biomeCache to avoid
	 * infinite loop in BiomeCacheBlock)
	 */
	public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] abiomegenbase, int i, int j, int k, int l, boolean flag)
	{
		IntCache.resetIntCache();

		if (abiomegenbase == null || abiomegenbase.length < k * l)
		{
			abiomegenbase = new BiomeGenBase[k * l];
		}

		BiomeGenBase biomegenbase = this.getBiomeGenAt(i, j);
		int i1 = 0;

		for (int j1 = 0; j1 < k; ++j1)
		{
			for (int k1 = 0; k1 < l; ++k1)
			{
				abiomegenbase[i1++] = biomegenbase;
			}
		}

		return abiomegenbase;
	}

	/**
	 * Finds a valid position within a range, that is in one of the listed
	 * biomes. Searches {par1,par2} +-par3 blocks. Strongly favors positive y
	 * positions.
	 */
	public ChunkPosition findBiomePosition(int i, int j, int k, List list, Random random)
	{
		return new ChunkPosition(0, 64, 0);
	}

	static
	{
		LinkedList linkedlist = new LinkedList();
		linkedlist.add(new BiomeEntry(BiomeGenBase.plains, 25));
		linkedlist.add(new BiomeEntry(BiomeGenBase.forest, 50));
		linkedlist.add(new BiomeEntry(BiomeGenBase.taiga, 40));
		linkedlist.add(new BiomeEntry(BiomeGenBase.desert, 25));
		linkedlist.add(new BiomeEntry(BiomeGenBase.icePlains, 25));
		linkedlist.add(new BiomeEntry(BiomeGenBase.jungle, 25));
		linkedlist.add(new BiomeEntry(BiomeGenBase.swampland, 40));
		linkedlist.add(new BiomeEntry(BiomeGenBase.hell, 10));
		linkedlist.add(new BiomeEntry(BiomeGenBase.mushroomIsland, 5));
		linkedlist.add(new BiomeEntry(BiomeGenBase.sky, 2));
		biomeList = Collections.unmodifiableList(linkedlist);
	}
}
