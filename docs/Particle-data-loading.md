There are several off-the-shelf options to get data in various formats into the `Gaia Sandbox`. These options can be organized into two main categories:

- **Local data**: The data to load are files in the local disk.
- **Object server**: The data will be loaded/streamed from an object server in the local machine or over the network.

![Particle data loading system](http://www.zah.uni-heidelberg.de/fileadmin/user_upload/gaia/gaiasandbox/img/diagrams/catalog-loading.png "Particle data loading system")

## Local data

In order to load **local data** there are a series of default options which can be combined. As described in the [[General data loading|General-information-on-the-data-loading-mechanisms]] section, multiple catalogue loaders can be used at once. Each catalog loader will get a list of files to load. A description of the main local catalog loaders follows.

### HYG catalog loaders
These loaders ([`HYGBinaryLoader`](/ari-zah/gaiasandbox/blob/master/core/src/gaia/cu9/ari/gaiaorbit/data/stars/HYGBinaryLoader.java) and [`HYGCSVLoader`](/ari-zah/gaiasandbox/blob/master/core/src/gaia/cu9/ari/gaiaorbit/data/stars/HYGCSVLoader.java)) load the HYG catalog that comes bundled with the Gaia Sandbox, which may be in `csv` format or in an arbitrary (not standard) binary -`bin`- format. Even though they have the `HYG-` prefix, these can load any file in the same format. The `csv` and `bin` formats are described below.
  - **`csv` format**: This is the `csv` format as downloaded from the [HYG Database site](http://www.astronexus.com/hyg). The first line contains the headers and is skipped. Then, each following row contains a particle (star) with the following columns:

   Name                             | Data type      | Optional  | Ignored
   ---------------------------------|----------------|-----------|---------
   Star ID (pk)                     | `long`         | no        | no
   Hipparcos catalog id             | `long`         | yes       | no
   Henry Draper catalog id          | `long`         | yes       | yes
   Harvard Revised catalog id       | `long`         | yes       | yes
   Gliese catalog id                | `string`       | yes       | yes
   Bayer / Flamsteed designation    | `string`       | yes       | no
   Proper name                      | `string`       | yes       | no
   Right ascension                  | `float` [deg]  | no        | no
   Declination                      | `float` [deg]  | no        | no
   Distance                         | `float` [pc]   | no        | no
   Magnitude                        | `float` [mag]  | no        | no
   Absolute magnitude               | `float` [mag]  | yes       | no
   Spectrum type                    | `string`       | no        | yes
   Color index                      | `float`        | no        | no
 
  - **`bin` format**: The binary format is described in the class comment of [`HYGBinaryLoader`](/ari-zah/gaiasandbox/blob/master/core/src/gaia/cu9/ari/gaiaorbit/data/stars/HYGBinaryLoader.java). The meaning of each single bit in this format is described below:

    - **32 bits (int)** - The number of stars in the file, `starNum`

    repeat the following `starNum` times (for each star)

    - **32 bits (int)** - The the length of the name, or `nameLength`
    - **16 bits * `nameLength` (chars)** - The name of the star
    - **32 bits (float)** - Apparent magnitude
    - **32 bits (float)** - Absolute magnitude
    - **32 bits (float)** - Color index B-V
    - **32 bits (float)** - Right ascension [deg]
    - **32 bits (float)** - Declination [deg]
    - **32 bits (float)** - Distance [pc * 3.0856775204864006E7]
    - **64 bits (long)** - Star identifier
  
    There is a utility to convert the `csv` catalog to the `bin` format. It is called [`HYGToBinary`](/ari-zah/gaiasandbox/blob/master/core/src/gaia/cu9/ari/gaiaorbit/data/stars/HYGToBinary.java) and it can easily be adapted to convert any supported format to this binary format.

### Octree catalog loader
This is practically the same format as the binary in the `HYGBinaryLoader` but adding some metadata to construct an [octree](http://en.wikipedia.org/wiki/Octree) in order to cull portions of the catalog that are not visible and to implement a level-of-detail system to reduce the amount of particles in the viewport. This loader needs two files, the **particles file** and the **metadata** file. Both files are binary files and their description is below.
  - **Particles file**: The actual reading and writing of the particles file is done in the [`ParticleDataBinaryIO`](/ari-zah/gaiasandbox/blob/master/core/src/gaia/cu9/ari/gaiaorbit/data/octreegen/ParticleDataBinaryIO.java). The format is exactly the same as in the HYG `bin` format but adding two extra attributes to each star which indicate the `pageId` (the identifier of the octant) and the `particleType`, an integer code indicating whether it is a real star or a virtual particle created for a higher LoD (level of detail).

    - **32 bits (int)** - The number of stars in the file, `starNum`

    repeat the following `starNum` times (for each star)

    - **32 bits (int)** - The the length of the name, or `nameLength`
    - **16 bits * `nameLength` (chars)** - The name of the star
    - **32 bits (float)** - Apparent magnitude
    - **32 bits (float)** - Absolute magnitude
    - **32 bits (float)** - Color index B-V
    - **32 bits (float)** - Right ascension [deg]
    - **32 bits (float)** - Declination [deg]
    - **32 bits (float)** - Distance [pc * 3.0856775204864006E7]
    - **64 bits (long)** - Star identifier
    - **64 bits (long)** - Page id
    - **32 bits (int)** - Particle type

  - **Metadata file**: This file contains the information of the Octree, its nodes -octants- and the particles each node contains. The reading and writing is handled by the [`MetadataBinaryIO`](/ari-zah/gaiasandbox/blob/master/core/src/gaia/cu9/ari/gaiaorbit/data/octreegen/MetadataBinaryIO.java). The format is as follows:

    - **32 bits (int)** with the number of nodes, `nNodes`

    repeat the following `nNodes` times (for each node)

    - **64 bits (long)** - `pageId` - The page id
    - **64 bits (double)** - `centreX` - The x component of the centre
    - **64 bits (double)** - `centreY` - The y component of the centre
    - **64 bits (double)** - `centreZ` - The z component of the centre
    - **64 bits (double)** - `sx` - The size in x
    - **64 bits (double)** - `sy` - The size in y
    - **64 bits (double)** - `sz` - The size in z
    - **64 bits * 8 (long)** - `childrenIds` - 8 longs with the ids of the children. If no child in the given position, the id is negative.
    - **32 bits (int)** - `depth` - The depth of the node
    - **32 bits (int)** - `nObjects` - The number of objects of this node and its descendants
    - **32 bits (int)** - `ownObjects` - The number of objects of this node
    - **32 bits (int)** - `childCount` - The number of children nodes

In order to produce these files from a catalog, one needs to [`OctreeGenerator`](/ari-zah/gaiasandbox/blob/master/core/src/gaia/cu9/ari/gaiaorbit/data/octreegen/OctreeGenerator.java). This class will get a list of stars and will produce the Octree according to certain parameters. The class [`OctreeGeneratorTest`](/ari-zah/gaiasandbox/blob/master/core/src/gaia/cu9/ari/gaiaorbit/data/octreegen/OctreeGeneratorTest.java) may be used to read a catalog from a file, generate the octree and write both the particles and the metadata files back to a file.

### STIL catalog loader
As of version `v0.704` the Gaia Sandbox supports all formats supported by the [`STIL` library](http://www.star.bristol.ac.uk/~mbt/stil/). Since the data held by the formats supported by `STIL` is not of a unique nature, this catalog loader makes a series of assumptions:

- Positional information exists in the source file (spherical/cartesian equatorial/galactic coordinates are accepted, correspoding to the `ucd`s `pos.eq.*` and `pos.galactic.*`, where the `*` can be `ra`, `dec`, `glat`, `glon`, `x`, `y` and `z`).
- Apparent magnitude data in at least one filter exists (`phot.mag;em.opt.*`, where `*` can be `V`, `B`, `I` or `R`).
- Absolute magnitude data is not required but always welcome (`phys.magAbs;em.opt.*`).
- B-V color index is present (corresponding to the `ucd` `phot.color;em.opt.B;em.opt.V`). More colors will be supported soon.
- If `meta.id` and/or `meta.id;meta.main` are present, they are used as name and identifier of the stars respectively. Otherwise, a random name and identifier are generated and assigned.

## Object server

Not implemented yet.