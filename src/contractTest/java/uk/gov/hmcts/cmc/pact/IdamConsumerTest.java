package uk.gov.hmcts.cmc.pact;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.model.RequestResponsePact;
import com.google.common.collect.Maps;
import io.restassured.RestAssured;
import org.apache.http.entity.ContentType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
public class IdamConsumerTest {

    private static final String IDAM_DETAILS_URL = "/details";
    private static final String IDAM_OPENID_TOKEN_URL = "/o/token";
    private static final String CLIENT_REDIRECT_URI = "/oauth2redirect";
    private static final String ACCESS_TOKEN = "111";

    @Pact(provider = "idam-api", consumer = "claim-store-api")//Producer pacticipant , consumer service
    public RequestResponsePact executeGetUserDetailsAndGet200(PactDslWithProvider builder) {

        Map<String, String> headers = Maps.newHashMap(); //create hashmap
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN); //

        return builder
            .given("Idam successfully returns user details")
            .uponReceiving("Provider receives a GET /details request from an claim-store API")
            .path(IDAM_DETAILS_URL)
            .method(HttpMethod.GET.toString())
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body(createUserDetailsResponse())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetUserDetailsAndGet200")// method - attempt- 200 ok, with MockServer
    public void should_get_user_details_with_access_token(MockServer mockServer) throws JSONException {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);
        String actualResponseBody =
            RestAssured
                .given()
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .when()
                .get(mockServer.getUrl() + IDAM_DETAILS_URL)
                .then()
                .statusCode(200)
                .and()
                .extract()
                .body()
                .asString();

        JSONObject response = new JSONObject(actualResponseBody);

        assertThat(actualResponseBody).isNotNull();
        assertThat(response).hasNoNullFieldsOrProperties();
        //Check non blank response Json fields
        assertThat(response.getString("id")).isNotBlank();
        assertThat(response.getString("forename")).isNotBlank();
        assertThat(response.getString("surname")).isNotBlank();

        JSONArray rolesArr = new JSONArray(response.getString("roles")); // create a rolesArray -
        // check assigned and below, prob better way of doing this in cMC
        assertThat(rolesArr).isNotNull(); //roles array is not blank
        assertThat(rolesArr.length()).isNotZero(); //roles array has item(s)
        assertThat(rolesArr.get(0).toString()).isNotBlank(); //first roles array item is not empty.

    }

    @Pact(provider = "idam-api", consumer = "claim-store-api")
    public RequestResponsePact executeGetIdamAccessTokenAndGet200(PactDslWithProvider builder) throws JSONException {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

        return builder
            .given("Idam successfully returns access token")
            .uponReceiving("Provider receives a POST /o/token request from an Stitching API")
            .path(IDAM_OPENID_TOKEN_URL)
            .method(HttpMethod.POST.toString())
            .body(createRequestBody(), ContentType.APPLICATION_FORM_URLENCODED)
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body(createAuthResponse())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetIdamAccessTokenAndGet200")
    public void should_post_to_token_endpoint_and_receive_access_token_with_200_response(MockServer mockServer)
        throws JSONException {

        String actualResponseBody =

            RestAssured
                .given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(createRequestBody())
                .log().all(true)
                .when()
                .post(mockServer.getUrl() + IDAM_OPENID_TOKEN_URL)
                .then()
                .statusCode(200)
                .and()
                .extract()
                .asString();

        JSONObject response = new JSONObject(actualResponseBody);

        assertThat(response).isNotNull();
        assertThat(response.getString("access_token")).isNotBlank();
        assertThat(response.getString("refresh_token")).isNotBlank();
        assertThat(response.getString("id_token")).isNotBlank();
        assertThat(response.getString("scope")).isNotBlank();
        assertThat(response.getString("token_type")).isEqualTo("Bearer");
        assertThat(response.getString("expires_in")).isNotBlank();

    }

    private PactDslJsonBody createAuthResponse() {

        return new PactDslJsonBody()
            .stringType("access_token", "some-long-value")
            .stringType("refresh_token", "another-long-value")
            .stringType("scope", "openid roles profile")
            .stringType("id_token", "some-value")
            .stringType("token_type", "Bearer")
            .stringType("expires_in","12345");

    }

    private PactDslJsonBody createUserDetailsResponse() {
        PactDslJsonArray array = new PactDslJsonArray().stringValue("caseofficer-em");

        return new PactDslJsonBody()
            .stringType("id", "123")
            .stringType("email", "em-caseofficer@fake.hmcts.net")
            .stringType("forename", "Case")
            .stringType("surname", "Officer")
            .stringType("roles", array.toString());

    }

    private static String createRequestBody() {

        return "{\"grant_type\": \"password\","
            + " \"client_id\": \"em\","
            + " \"client_secret\": \"some_client_secret\","
            + " \"redirect_uri\": \"/oauth2redirect\","
            + " \"scope\": \"openid roles profile\","
            + " \"username\": \"stitchingusername\","
            + " \"password\": \"stitchingpwd\"\n"
            + " }";
    }


}

