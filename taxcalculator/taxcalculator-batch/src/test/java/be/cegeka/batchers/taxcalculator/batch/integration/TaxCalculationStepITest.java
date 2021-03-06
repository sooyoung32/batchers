package be.cegeka.batchers.taxcalculator.batch.integration;

import be.cegeka.batchers.taxcalculator.application.domain.Employee;
import be.cegeka.batchers.taxcalculator.application.domain.EmployeeRepository;
import be.cegeka.batchers.taxcalculator.application.domain.EmployeeTestBuilder;
import be.cegeka.batchers.taxcalculator.batch.config.singlejvm.EmployeeJobConfigSingleJvm;
import be.cegeka.batchers.taxcalculator.batch.domain.TaxCalculation;
import be.cegeka.batchers.taxcalculator.batch.domain.TaxCalculationRepository;
import org.junit.After;
import org.junit.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class TaxCalculationStepITest extends AbstractBatchIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TaxCalculationRepository taxCalculationRepository;

    @After
    public void tearDown() {
        taxCalculationRepository.deleteAll();
        employeeRepository.deleteAll();
    }

    @Test
    public void taxCalculationStep_noWork() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("year", 2014L, true)
                .addLong("month", 5L, true)
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchStep(EmployeeJobConfigSingleJvm.TAX_CALCULATION_STEP, jobParameters);

        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertThat(taxCalculationRepository.find(2014, 5, 1L)).isEmpty();
    }

    @Test
    public void taxCalculationStep_generatesCorrectCalculation() throws Exception {
        Employee employee = haveOneEmployee();

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("year", 2014L, true)
                .addLong("month", 5L, true)
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchStep(EmployeeJobConfigSingleJvm.TAX_CALCULATION_STEP, jobParameters);

        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        List<TaxCalculation> byEmployee = taxCalculationRepository.findByEmployee(employee);

        assertThat(byEmployee).hasSize(1);
        TaxCalculation taxCalculation = byEmployee.get(0);
        assertThat(taxCalculation.getEmployee().getId()).isEqualTo(employee.getId());
        assertThat(taxCalculation.getYear()).isEqualTo(2014);
        assertThat(taxCalculation.getMonth()).isEqualTo(5);

        List<TaxCalculation> byYearAndMonth = taxCalculationRepository.find(2014, 5, 1L);
        assertThat(byYearAndMonth).hasSize(1);
    }

    private Employee haveOneEmployee() {
        Employee employee = new EmployeeTestBuilder()
                .withFirstName("Monica")
                .withLastName("Dev")
                .withIncome(1000)
                .withEmailAddress("monica@cegeka.com")
                .build();

        employeeRepository.save(employee);
        return employee;
    }

}