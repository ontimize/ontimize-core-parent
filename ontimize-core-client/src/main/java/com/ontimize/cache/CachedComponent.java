package com.ontimize.cache;

import java.util.List;

public interface CachedComponent {

	public String getEntity();

	public List<Object> getAttributes();

	public void setCacheManager(CacheManager c);

}
