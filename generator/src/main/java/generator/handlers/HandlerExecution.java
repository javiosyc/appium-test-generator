package generator.handlers;

import java.util.Map;

public interface HandlerExecution<T> {

	public void addRecordTo(Map<String, Object> store);

	/**
	 * 定義讀取規則
	 */
	public void generate();

	public T getData();

	public Object getDataFrom(Map<String, Object> store);

	public String getName();
}
