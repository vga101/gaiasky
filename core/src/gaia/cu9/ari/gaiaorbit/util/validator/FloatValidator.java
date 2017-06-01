package gaia.cu9.ari.gaiaorbit.util.validator;

public class FloatValidator implements IValidator {

    private float min;
    private float max;

    public FloatValidator() {
	this(Float.MIN_VALUE, Float.MAX_VALUE);
    }

    public FloatValidator(float min, float max) {
	super();
	this.min = min;
	this.max = max;
    }

    public boolean validate(String value) {
	Float val = null;
	try {
	    val = Float.parseFloat(value);
	} catch (NumberFormatException e) {
	    return false;
	}

	return val >= min && val <= max;
    }

}
