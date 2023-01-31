package com.ardaslegends.presentation.abstraction;

import com.ardaslegends.presentation.AbstractRestController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
public class ControllerUnitTest extends RestTest<MvcResult>{

    private MockMvc mockMvc;

    protected void baseSetup(AbstractRestController controller, String baseUrl) {
        super.baseSetup(controller, baseUrl, 8080);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    protected MvcResult post(String path, Object data) throws Exception{
        return post(path, data, null);
    }

    protected MvcResult patch(String path, Object data) throws Exception{
        return patch(path, data, null);
    }

    protected MvcResult delete(String path, Object data) throws Exception{
        return delete(path, data, null);
    }

    protected MvcResult get(String path, Object data) throws Exception{
        return get(path, data, null);
    }

    @Override
    <T> MvcResult post(String path, Object data, Class<T> responseType) throws Exception{
        log.trace("Building JSON from data");
        String requestJson = ow.writeValueAsString(data);

        log.debug("Performing Post request to {}", url + path);
        return mockMvc.perform(MockMvcRequestBuilders
                        .post(url + path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
    }

    @Override
    <T> MvcResult patch(String path, Object data, Class<T> responseType) throws Exception{
        log.trace("Building JSON from data");
        String requestJson = ow.writeValueAsString(data);

        log.debug("Performing Patch request to {}", url + path);
        return mockMvc.perform(MockMvcRequestBuilders
                        .patch(url + path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
    }

    @Override
    <T> MvcResult delete(String path, Object data, Class<T> responseType) throws Exception{
        log.trace("Building JSON from data");
        String requestJson = ow.writeValueAsString(data);

        log.debug("Performing Delete request to {}", url + path);
        return mockMvc.perform(MockMvcRequestBuilders
                        .delete(url + path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
    }

    @Override
    <T> MvcResult get(String path, Object data, Class<T> responseType) throws Exception{
        log.trace("Building JSON from data");
        String requestJson = ow.writeValueAsString(data);

        log.debug("Performing Get request to {}", url + path);
        return mockMvc.perform(MockMvcRequestBuilders
                        .get(url + path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
    }


}