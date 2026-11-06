package org.delcom.ifs25026pbo2026springbootcashflow;

import org.delcom.ifs25026pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25026pbo2026springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25026pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest
class AppTest {

    @Autowired
    private ITransactionRepository transactionRepository;

    @Autowired
    private FinanceUseCase financeUseCase;

    @Autowired
    private FinancePresenter financePresenter;

    @Test
    void contextLoadsAndCleanArchitectureWiringIsComplete() {
        assertNotNull(transactionRepository);
        assertNotNull(financeUseCase);
        assertNotNull(financePresenter);
    }

    @Test
    void appMainDelegatesToSpringApplicationRun() throws Exception {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            App.main(new String[0]);
            mocked.verify(() -> SpringApplication.run(eq(App.class), any(String[].class)));
        }
    }
}
