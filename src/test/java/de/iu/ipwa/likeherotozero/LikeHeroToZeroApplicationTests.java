package de.iu.ipwa.likeherotozero;

import de.iu.ipwa.likeherotozero.service.EmissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:likehero-tests;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class LikeHeroToZeroApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void convertsMegatonnesToKilotonnesWithoutLosingPrecision() {
        assertThat(EmissionService.megatonnesToKilotonnes(new BigDecimal("579.9356")))
                .isEqualByComparingTo("579935.6000");
    }

    @Test
    void publicLatestValueIsVisibleAndScientistCanCorrectIt() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Germany · 2024")))
                .andExpect(content().string(containsString("579.935,6")));

        mockMvc.perform(get("/scientist"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        MvcResult login = mockMvc.perform(formLogin()
                        .user("scientist")
                        .password("local-demo-change-me"))
                .andExpect(authenticated().withUsername("scientist"))
                .andExpect(redirectedUrl("/scientist"))
                .andReturn();

        mockMvc.perform(post("/scientist/emissions")
                        .session((MockHttpSession) login.getRequest().getSession(false))
                        .with(csrf())
                        .param("iso3", "DEU")
                        .param("countryName", "Germany")
                        .param("year", "2024")
                        .param("co2Kt", "600000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/scientist"));

        mockMvc.perform(get("/").param("country", "DEU"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("600.000,0")));
    }
}
