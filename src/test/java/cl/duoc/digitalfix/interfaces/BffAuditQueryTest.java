package cl.duoc.digitalfix.interfaces;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

class BffAuditQueryTest {
    @Test
    void forwardsDecodedAuditFiltersWithoutChangingTheirValues() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        BffController controller = new BffController(builder, "http://orders", "http://catalog", "http://report", "http://audit");
        var params = new LinkedMultiValueMap<String, String>();
        params.add("userId", "ana+ventas@empresa.cl");
        params.add("eventType", "WORK ORDER");
        server.expect(requestTo("http://audit/api/audit?userId=ana%2Bventas%40empresa.cl&eventType=WORK%20ORDER"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        controller.searchAudit(params, new HttpHeaders());
        server.verify();
    }
}
