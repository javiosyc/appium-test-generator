package generator.handlers;

import java.util.Map;

public interface HandlerExecution<T> {

	public void addRecordTo(Map<String, Object> store);

	public void generate();

	public T getData();

	public Object getDataFrom(Map<String, Object> store);

	public String getName();
}
