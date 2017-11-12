package generator.handlers;

import java.util.List;
import java.util.Map;

public interface HandlerExecution<T> {

	public void generate();

	public String getName();

	public void addRecordTo(Map<String, Object> store);

	public Object getDataFrom(Map<String, Object> store);
	
	public T getData();
}
