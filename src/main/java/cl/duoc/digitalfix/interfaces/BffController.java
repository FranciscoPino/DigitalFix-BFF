package cl.duoc.digitalfix.interfaces;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api")
public class BffController {
    private final RestClient.Builder restClientBuilder;
    private final String workordersUrl;
    private final String catalogUrl;
    private final String reportUrl;
    private final String auditUrl;

    public BffController(
            RestClient.Builder restClientBuilder,
            @Value("${app.services.workorders-url}") String workordersUrl,
            @Value("${app.services.catalog-url}") String catalogUrl,
            @Value("${app.services.report-url}") String reportUrl,
            @Value("${app.services.audit-url}") String auditUrl) {
        this.restClientBuilder = restClientBuilder;
        this.workordersUrl = workordersUrl;
        this.catalogUrl = catalogUrl;
        this.reportUrl = reportUrl;
        this.auditUrl = auditUrl;
    }

    @GetMapping("/workorders")
    public ResponseEntity<JsonNode> listWorkOrders(
            @RequestParam MultiValueMap<String, String> queryParams,
            @RequestHeader HttpHeaders headers) {
        return forward(HttpMethod.GET, workordersUrl + "/api/workorders", queryParams, null, headers);
    }

    @PostMapping("/workorders")
    public ResponseEntity<JsonNode> createWorkOrder(
            @RequestBody JsonNode body,
            @RequestHeader HttpHeaders headers) {
        return forward(HttpMethod.POST, workordersUrl + "/api/workorders", null, body, headers);
    }

    @GetMapping("/workorders/{id}")
    public ResponseEntity<JsonNode> getWorkOrder(
            @PathVariable String id,
            @RequestHeader HttpHeaders headers) {
        return forward(HttpMethod.GET, workordersUrl + "/api/workorders/" + id, null, null, headers);
    }

    @PutMapping("/workorders/{id}/status")
    public ResponseEntity<JsonNode> changeWorkOrderStatus(
            @PathVariable String id,
            @RequestBody JsonNode body,
            @RequestHeader HttpHeaders headers) {
        return forward(HttpMethod.PUT, workordersUrl + "/api/workorders/" + id + "/status", null, body, headers);
    }

    // Solo demostración: provoca un mensaje inválido que termina en la DLQ de notify
    @PostMapping("/workorders/demo/dlq")
    public ResponseEntity<JsonNode> demoDlq(
            @RequestParam MultiValueMap<String, String> queryParams,
            @RequestHeader HttpHeaders headers) {
        return forward(HttpMethod.POST, workordersUrl + "/api/workorders/demo/dlq", queryParams, null, headers);
    }

    @GetMapping("/catalog/services")
    public ResponseEntity<JsonNode> listServices(@RequestHeader HttpHeaders headers) {
        return forward(HttpMethod.GET, catalogUrl + "/api/catalog/services", null, null, headers);
    }

    @PostMapping("/catalog/services")
    public ResponseEntity<JsonNode> createService(
            @RequestBody JsonNode body,
            @RequestHeader HttpHeaders headers) {
        return forward(HttpMethod.POST, catalogUrl + "/api/catalog/services", null, body, headers);
    }

    @PutMapping("/catalog/services/{id}")
    public ResponseEntity<JsonNode> updateService(
            @PathVariable String id,
            @RequestBody JsonNode body,
            @RequestHeader HttpHeaders headers) {
        return forward(HttpMethod.PUT, catalogUrl + "/api/catalog/services/" + id, null, body, headers);
    }

    @GetMapping("/catalog/spare-parts")
    public ResponseEntity<JsonNode> listSpareParts(@RequestHeader HttpHeaders headers) {
        return forward(HttpMethod.GET, catalogUrl + "/api/catalog/spare-parts", null, null, headers);
    }

    @PostMapping("/catalog/spare-parts")
    public ResponseEntity<JsonNode> createSparePart(@RequestBody JsonNode body, @RequestHeader HttpHeaders headers) {
        return forward(HttpMethod.POST, catalogUrl + "/api/catalog/spare-parts", null, body, headers);
    }

    @PutMapping("/catalog/spare-parts/{id}")
    public ResponseEntity<JsonNode> updateSparePart(@PathVariable String id, @RequestBody JsonNode body,
                                                     @RequestHeader HttpHeaders headers) {
        return forward(HttpMethod.PUT, catalogUrl + "/api/catalog/spare-parts/" + id, null, body, headers);
    }

    @GetMapping("/report/kpis")
    public ResponseEntity<JsonNode> kpis(
            @RequestParam MultiValueMap<String, String> queryParams,
            @RequestHeader HttpHeaders headers) {
        return forward(HttpMethod.GET, reportUrl + "/api/report/kpis", queryParams, null, headers);
    }

    @GetMapping("/report/top-services")
    public ResponseEntity<JsonNode> topServices(
            @RequestParam MultiValueMap<String, String> queryParams,
            @RequestHeader HttpHeaders headers) {
        return forward(HttpMethod.GET, reportUrl + "/api/report/top-services", queryParams, null, headers);
    }

    @GetMapping("/audit/{workOrderId}")
    public ResponseEntity<JsonNode> auditByWorkOrder(
            @PathVariable String workOrderId,
            @RequestHeader HttpHeaders headers) {
        return forward(HttpMethod.GET, auditUrl + "/api/audit/" + workOrderId, null, null, headers);
    }

    @GetMapping("/audit")
    public ResponseEntity<JsonNode> searchAudit(
            @RequestParam MultiValueMap<String, String> queryParams,
            @RequestHeader HttpHeaders headers) {
        return forward(HttpMethod.GET, auditUrl + "/api/audit", queryParams, null, headers);
    }

    private ResponseEntity<JsonNode> forward(
            HttpMethod method,
            String baseUrl,
            MultiValueMap<String, String> queryParams,
            JsonNode body,
            HttpHeaders incomingHeaders) {
        URI target = buildUri(baseUrl, queryParams);
        RestClient.RequestBodySpec request = restClientBuilder.build()
                .method(method)
                .uri(target)
                .headers(headers -> copyAuthorization(incomingHeaders, headers));

        if (body != null) {
            return request.body(body).retrieve().toEntity(JsonNode.class);
        }
        return request.retrieve().toEntity(JsonNode.class);
    }

    private URI buildUri(String baseUrl, MultiValueMap<String, String> queryParams) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl);
        if (queryParams != null) {
            queryParams.forEach((key, values) -> values.forEach(value -> builder.queryParam(key, value)));
        }
        return builder.build(true).toUri();
    }

    private void copyAuthorization(HttpHeaders incoming, HttpHeaders outgoing) {
        Optional.ofNullable(incoming.getFirst(HttpHeaders.AUTHORIZATION))
                .ifPresent(value -> outgoing.set(HttpHeaders.AUTHORIZATION, value));
        outgoing.set(HttpHeaders.ACCEPT, "application/json");
    }
}
