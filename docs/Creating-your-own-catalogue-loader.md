In order to create a loader for your catalogue, one only needs to provide an implementation to the [`ICatalogLoader`](/ari-zah/gaiasandbox/blob/master/core/src/gaia/cu9/ari/gaiaorbit/data/stars/ICatalogLoader.java) interface.

``` java
public interface ICatalogLoader {
    public List<? extends SceneGraphNode> loadCatalog() throws FileNotFoundException;
    public void initialize(Properties p);
}
```
The main method to implement is [`List<? extends SceneGraphNode> loadCatalog()`](/ari-zah/gaiasandbox/blob/master/core/src/gaia/cu9/ari/gaiaorbit/data/stars/ICatalogLoader.java#L11), which must return a list of elements that extend `SceneGraphNode`, usually `Star`s.

But how do we know which file to load? You need to create a `catalog-*.json` file, add your loader there and create the properties you desire. Usually, there is a property called `files` which contains a list of files to load. Once you've done that, implement the [`initialize(Properties p)`](/ari-zah/gaiasandbox/blob/master/core/src/gaia/cu9/ari/gaiaorbit/data/stars/ICatalogLoader.java#L13) method knowing that all the properties defined in the `catalog-*.json` file with your catalogue loader as a prefix will be passed in the `Properties p` object without prefix.

Also, you will need to connect this new catalog file with the Gaia Sky configuration so that it is loaded at startup. To do so, locate your `global.properties` file (usually under `$HOME/.gaiasky/`) and update the property `data.json.catalog` with your catalog json file.

Add your implementing `jar` file to the `classpath` and you are good to go.

Take a look at already implemented catalogue loaders such as the [`OctreeCatalogLoader`](/ari-zah/gaiasandbox/blob/master/core/src/gaia/cu9/ari/gaiaorbit/data/stars/OctreeCatalogLoader.java) to see how it works.
