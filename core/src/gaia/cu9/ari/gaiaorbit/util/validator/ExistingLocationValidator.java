package gaia.cu9.ari.gaiaorbit.util.validator;

import gaia.cu9.ari.gaiaorbit.scenegraph.Loc;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;

public class ExistingLocationValidator implements IValidator {

    SceneGraphNode parent;

    public ExistingLocationValidator(SceneGraphNode parent) {
	this.parent = parent;
    }

    @Override
    public boolean validate(String value) {
	return value != null && !value.isEmpty() && parent.getChildByNameAndType(value, Loc.class) != null;
    }

}
