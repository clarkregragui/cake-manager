package com.waracle.cakes.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.waracle.cakes.model.dto.CakeDTO;
import com.waracle.cakes.service.CakeService;

@ActiveProfiles("test")
@CamelSpringBootTest
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class CakeAppTest {
	
	
    @Autowired
    private TestRestTemplate restTemplate;

    
    @SpyBean
    private CakeService cakeService;
        
    private static String expectedCakesView;
    
    private static List<CakeDTO> expectedCakesList;
    
    @BeforeAll
    public static void beforeClass() throws IOException {
    	Resource cakesViewFileResource = new ClassPathResource("expectedCakesView.html");
    	expectedCakesView = Files.contentOf(cakesViewFileResource.getFile(), StandardCharsets.UTF_8);
    	expectedCakesView.trim();
    	
    	Resource cakesJsonFileResource = new ClassPathResource("cakes.json");
    	String json = Files.contentOf(cakesJsonFileResource.getFile(), StandardCharsets.UTF_8);
    	ObjectMapper mapper = new ObjectMapper();
    	expectedCakesList = mapper.readValue(json, new TypeReference<List<CakeDTO>>(){});
    	

    }
    
    
    @Test
    public void getCakesTest() {
        ResponseEntity<List<CakeDTO>> response = restTemplate.exchange("/cakes",
            HttpMethod.GET, null, new ParameterizedTypeReference<List<CakeDTO>>() {
            });
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<CakeDTO> cakes = response.getBody();
        assertThat(cakes).hasSize(5);
        assertThat(cakes).containsAll(expectedCakesList);
    }
    
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void postCreateCakeTest() {

    	CakeDTO newCake = new CakeDTO();
    	newCake.setTitle("Chocolate cake");
    	newCake.setDesc("Very chocolatey");
    	newCake.setImage("chocolate_cake_img.jpeg");

    	HttpHeaders headers = new HttpHeaders();

    	headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    	HttpEntity<CakeDTO> request = new HttpEntity<CakeDTO>(newCake, headers);

    	ResponseEntity<CakeDTO> postResponse = restTemplate.exchange("/cakes",
    			HttpMethod.POST, request, new ParameterizedTypeReference<CakeDTO>() {
    	});
    	
    	assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    	
    	
    	ResponseEntity<List<CakeDTO>> getResponse = restTemplate.exchange("/cakes",
    			HttpMethod.GET, null, new ParameterizedTypeReference<List<CakeDTO>>() {
    	});

    	assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    	List<CakeDTO> cakes = getResponse.getBody();
    	assertThat(cakes).hasSize(6);
    	assertThat(cakes).contains(newCake);
    	
    }
    
    @Test
    public void getCakesViewTest() {
    	
    	ResponseEntity<String> getResponse = restTemplate.exchange("/",
    			HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
    	});
    	
    	assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    	assertThat(getResponse.getBody().trim()).isEqualTo(expectedCakesView.trim());
    	
    }
    
    /* Error cases*/
    @Test
    public void postBadRequestUniqueConstraintViolationTest() {
    	
    	CakeDTO existingCake = new CakeDTO();
    	existingCake.setTitle("Lemon cheesecake");
    	existingCake.setDesc("A cheesecake made of lemon");
    	existingCake.setImage("https://s3-eu-west-1.amazonaws.com/s3.mediafileserver.co.uk/carnation/WebFiles/RecipeImages/lemoncheesecake_lg.jpg");

    	ResponseEntity<String> postResponse = restTemplate.postForEntity("/cakes", existingCake, String.class);
    	
    	assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
    
    @Test
    public void postBadRequestMissingFieldsTest() {
    	
    	CakeDTO existingCake = new CakeDTO();
    	existingCake.setDesc("A cheesecake made of lemon");
    	existingCake.setImage("https://s3-eu-west-1.amazonaws.com/s3.mediafileserver.co.uk/carnation/WebFiles/RecipeImages/lemoncheesecake_lg.jpg");

    	ResponseEntity<String> postResponse = restTemplate.postForEntity("/cakes", existingCake, String.class);
    	
    	assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
    
    @Test
    public void postBadRequestInvalidJsonTest() {
    	
    	//json missing comma
    	String invalidCakeJson = "{\n"
    			+ "  \"title\" : \"Lemon cheesecake\"\n"
    			+ "  \"desc\" : \"A cheesecake made of lemon\",\n"
    			+ "  \"image\" : \"https://s3-eu-west-1.amazonaws.com/s3.mediafileserver.co.uk/carnation/WebFiles/RecipeImages/lemoncheesecake_lg.jpg\"\n"
    			+ "}";
    	
    	ResponseEntity<String> postResponse = restTemplate.postForEntity("/cakes", invalidCakeJson, String.class);
    	
    	assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
    
    @Test
    public void getServerError500Test() {
    	
    	Mockito.when(cakeService.findCakes()).thenThrow(RuntimeException.class);
    	
        ResponseEntity<String> errorResponse = restTemplate.exchange("/cakes",
                HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
                });
        
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
