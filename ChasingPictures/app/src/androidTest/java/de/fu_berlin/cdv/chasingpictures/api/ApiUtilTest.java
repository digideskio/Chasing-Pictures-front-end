package de.fu_berlin.cdv.chasingpictures.api;

import android.app.Application;
import android.test.ApplicationTestCase;

import java.net.URI;

import de.fu_berlin.cdv.chasingpictures.R;

/**
 * @author Simon
 */
public class ApiUtilTest extends ApplicationTestCase<Application> {

    private ApiUtil apiUtil;

    public ApiUtilTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        apiUtil = new ApiUtil(getContext());
    }

    @Override
    protected void tearDown() throws Exception {

    }

    public void testCallSuccessful() throws Exception {

    }

    public void testGetURIforEndpoint() throws Exception {
        String base_uri = getContext().getString(R.string.api_url);

        URI expectedUriRegister = new URI(base_uri + getContext().getString(R.string.api_path_register));
        assertEquals("Registration URIs differ", expectedUriRegister, apiUtil.getURIforEndpoint(R.string.api_path_register));

        URI expectedUriLogin = new URI(base_uri + getContext().getString(R.string.api_path_login));
        assertEquals("Login URIs differ", expectedUriLogin, apiUtil.getURIforEndpoint(R.string.api_path_login));
    }
}