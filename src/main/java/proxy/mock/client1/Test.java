package proxy.mock.client1;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

public class Test {

	public static void main(String[] args) {
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		asyncHttpClient.prepareGet("http://www.ning.com/").execute(
				new AsyncCompletionHandler<Response>() {

					@Override
					public Response onCompleted(Response response)
							throws Exception {
						// Do something with the Response
						// ...
						System.out.print(response);
						return response;
					}

					@Override
					public void onThrowable(Throwable t) {
						// Something wrong happened.
					}
				});
	}
}
