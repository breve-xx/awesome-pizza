package org.altervista.breve.awesome.pizza.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ExtendWith({SpringExtension.class})
@ContextConfiguration(classes = {HomeController.class})
@TestPropertySource(properties = """
        awesome.pizza.api.name = an-api-name
        awesome.pizza.api.version = an-api-version
        """)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldSayHello() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello from an-api-name an-api-version"));
    }
}