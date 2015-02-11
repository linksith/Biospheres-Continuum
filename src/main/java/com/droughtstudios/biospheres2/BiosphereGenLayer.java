package com.droughtstudios.biospheres2;

import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by Trevor on 2/8/2015.
 */
public class BiosphereGenLayer extends GenLayer {

	private static final int DIR_LEFT = 0;
	private static final int DIR_RIGHT = 1;
	private static final int DIR_ABOVE = 2;
	private static final int DIR_BELOW = 3;

	public BiosphereGenLayer(long p_i2125_1_, GenLayer genLayer) {
		super(p_i2125_1_);
		parent = genLayer;
	}

	@Override
	public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
		int[] rawBiomes = parent.getInts(areaX, areaY, areaWidth, areaHeight);
		int[] sphereBiomes = IntCache.getIntCache(areaWidth * areaHeight);

		// start crawling regions
		RegionCrawler regionCrawler = new RegionCrawler(areaWidth, areaHeight, rawBiomes);
		regionCrawler.startCrawlAt(0, 0);

		// fill with empty biome
		Arrays.fill(sphereBiomes, EmptyBiomeGenBase.id);

		// fill in each biome region with point-radius circle
		for (BiomeRegion region : regionCrawler.getFinishedRegions()) {
			// only use major regions
			if (region.radius == 0) continue;

			// iterate through region's space
			int x, y;
			for (x = Math.max(region.centerX - region.radius, 0);
			     x <= Math.min(region.centerX + region.radius, areaWidth - 1);
			     x++) {

				for (y = Math.max(region.centerY - region.radius, 0);
				     y <= Math.min(region.centerY - region.radius, areaHeight - 1);
				     y++) {

					// equation of a circle
					int a = x - region.centerX;
					int b = y - region.centerY;

					// point is inside the circle
					if ((a * a) + (b * b) <= region.radius * region.radius) {
						sphereBiomes[convertCoord(x, y, areaWidth)] = region.biome;
					}
				}
			}
		}

		return sphereBiomes;
	}

	private static int convertCoord(int x, int y, int width) {
		return y * width + x;
	}

	/**
	 * order of ops
	 * 1 crawl full biome
	 * 2 set biome borders
	 * 3 calc biome centerpoint and radius
	 * 4 crawl border biomes -> go to 1
	 */
	private class RegionCrawler {
		private final int mWidth;
		private final int mHeight;
		private final int[] mRawBiomes;
		private final BiomeRegion[][] mRegions;

		private List<BiomeRegion> mFinishedRegions = new ArrayList<BiomeRegion>();

		public RegionCrawler(int width, int height, int[] rawBiomes) {
			mWidth = width;
			mHeight = height;
			mRawBiomes = rawBiomes;
			mRegions = new BiomeRegion[width][height];
		}

		public List<BiomeRegion> getFinishedRegions() {
			return mFinishedRegions;
		}

		public void startCrawlAt(int x, int y) {
			int biome = mRawBiomes[convertCoord(x, y, mWidth)];

			// create the region from this biome
			BiomeRegion region = new BiomeRegion(biome);
			mRegions[x][y] = region;

			// fully crawl this region
			crawlRegion(x, y);

			// calculate centerpoint and radius of region
			region.finish();

			mFinishedRegions.add(region);

			// crawl other regions
			crawlBorders(region);
		}

		private void crawlBorders(BiomeRegion curRegion) {
			// iterate allowing removal during crawls
			Set<Point> borderCopy = new HashSet<Point>(curRegion.border);
			for (Point point : borderCopy) {
				// if this point is not in the region's border, it was removed during crawl; skip
				if (!curRegion.border.contains(point))
					continue;

				// sanity check
				if (mRegions[point.x][point.y] == null) {
					startCrawlAt(point.x, point.y);
				}
			}
		}

		private void crawlRegion(int x, int y) {
			BiomeRegion curRegion = mRegions[x][y];

			// check left
			if (x > 0) {
				testSpace(x, y, DIR_LEFT, curRegion);
			}
			// check right
			if (x < mWidth - 1) {
				testSpace(x, y, DIR_RIGHT, curRegion);
			}
			// check above
			if (y > 0) {
				testSpace(x, y, DIR_ABOVE, curRegion);
			}
			// check below
			if (y < mHeight - 1) {
				testSpace(x, y, DIR_BELOW, curRegion);
			}
		}

		private void testSpace(int x, int y, int direction, BiomeRegion curRegion) {

			int cx = x;
			int cy = y;

			switch (direction) {
				case DIR_LEFT:
					cx = x - 1;
					break;
				case DIR_RIGHT:
					cx = x + 1;
					break;
				case DIR_ABOVE:
					cy = y - 1;
					break;
				case DIR_BELOW:
					cy = y + 1;
					break;
			}

			BiomeRegion testRegion = mRegions[cx][cy];

			// this biome is already part of a region
			if (testRegion != null) {
				// this biome is NOT part of the current region
				if (testRegion != curRegion) {
					// remove this space from the border of the other biome, so it does not try to explore this space in the future
					testRegion.border.remove(new Point(x, y));
				}
				return;
			}

			// this biome is not yet part of a region
			int biome = mRawBiomes[convertCoord(cx - 1, cy, mWidth)];

			// same biome! assimilate!
			if (biome == curRegion.biome) {
				mRegions[cx][cy] = curRegion;

				// set region borders
				if (cx < curRegion.left) curRegion.left = cx;
				if (cx > curRegion.right) curRegion.right = cx;
				if (cy < curRegion.top) curRegion.top = cy;
				if (cy > curRegion.bottom) curRegion.bottom = cy;

				// continue crawling from here
				crawlRegion(cx, cy);
			}

			// not the same biome, so add this border to the current region
			else {
				curRegion.border.add(new Point(cx, cy));
			}
		}
	}

	private class BiomeRegion {
		int biome;
		int left = Integer.MAX_VALUE;
		int right = Integer.MIN_VALUE;
		int top = Integer.MAX_VALUE;
		int bottom = Integer.MIN_VALUE;

		int centerX;
		int centerY;
		int radius;

		Set<Point> border;

		public BiomeRegion(int biome) {
			this.biome = biome;
			border = new HashSet<Point>();
		}

		public void finish() {
			// todo find a better algorithm; naive average for now
			centerX = left + (right - left) / 2;
			centerY = top + (bottom - top) / 2;

			// get radius from borders
			int minDistanceSq = Integer.MAX_VALUE;
			for (Point point : border) {
				int distance = (int) point.distanceSq(centerX, centerY);

				if (distance < minDistanceSq) {
					minDistanceSq = distance;
				}
			}

			radius = (int) Math.sqrt(minDistanceSq);
		}
	}
}
