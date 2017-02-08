package us.blockbox.shopui;

public abstract class CachedObject<T>{
	protected boolean valid;
	private T value;

	public CachedObject(){
		this.value = null;
		this.valid = false;
	}

	public CachedObject(T value){
		this.value = value;
		this.valid = true;
	}

	protected void setValue(T t){
		this.value = t;
	}

	public final T getValue(){
		if(!valid){
			validate();
		}
		return value;
	}

	public void invalidate(){
		this.valid = false;
	}

	protected abstract void validate();
}