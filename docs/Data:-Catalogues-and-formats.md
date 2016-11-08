The Gaia Sky needs to first load data in order to display it. The internal structure of these data is a `scenegraph`, which is basically a **tree with nodes**. The objects that are displayed in a scene are all nodes in this scene graph and are organized in a hierarchical manner depending on their geometrical and spatial relations.

The data nodes in the scene graph are of multiple natures and are *loaded differently depending on their type*. Here we can make the first big distinction in the data nodes depending on where they come from:
- **Particle data**: usually stars which come from a star catalogue.
- **Rest of data**: planets, orbits, constellations, grids and everything else qualifies for this category.

Data belonging to either group will be loaded differently into the Gaia Sky. The sections below describe the data format in detail:

- [[General information on the data loading mechanisms]]
- [[Particle data: loading catalogs|particle-data-loading]]
- [[Non-particle data: planets, orbits, grids, etc.|Non-particle-data-loading]]
- [[Creating your own catalogue loader|creating-your-own-catalogue-loader]]
- [[Loading from scripts]]
