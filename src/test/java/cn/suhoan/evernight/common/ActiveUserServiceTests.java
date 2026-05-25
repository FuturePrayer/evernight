package cn.suhoan.evernight.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ActiveUserServiceTests {

    @Test
    void countUniqueClientsWithinWindow() {
        ActiveUserProperties properties = new ActiveUserProperties();
        ActiveUserService service = new ActiveUserService(properties);

        service.record("1.1.1.1");
        service.record("1.1.1.1");
        service.record("2.2.2.2");

        assertThat(service.count()).isEqualTo(2);
    }

    @Test
    void countReturnsZeroWhenDisabled() {
        ActiveUserProperties properties = new ActiveUserProperties();
        properties.setEnabled(false);
        ActiveUserService service = new ActiveUserService(properties);

        service.record("1.1.1.1");

        assertThat(service.count()).isZero();
    }

}
