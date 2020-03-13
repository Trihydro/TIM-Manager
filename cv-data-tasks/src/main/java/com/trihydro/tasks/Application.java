package com.trihydro.tasks;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.JavaMailSenderImplProvider;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.library.service.SdwService;
import com.trihydro.tasks.actions.CleanupActiveTims;
import com.trihydro.tasks.actions.RemoveExpiredActiveTims;
import com.trihydro.tasks.actions.ValidateSDX;
import com.trihydro.tasks.config.DataTasksConfiguration;
import com.trihydro.tasks.config.EmailConfiguration;

import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ SdwService.class, Utility.class, EmailHelper.class, JavaMailSenderImplProvider.class, ActiveTimService.class })
public class Application {

	protected static DataTasksConfiguration configuration;
	private Utility utility;

    private ApplicationContext appContext;

	// TODO: remove from here
	private ValidateSDX sdxValidator;

	@Autowired
	public void InjectDependencies(DataTasksConfiguration configurationRhs, Utility _utility,
			ValidateSDX _sdxValidator, ApplicationContext _appContext) {
		configuration = configurationRhs;
		utility = _utility;
		sdxValidator = _sdxValidator;
		appContext = _appContext;

		CvDataServiceLibrary.setCVRestUrl(configuration.getCvRestService());
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@PostConstruct
	public void run() throws IOException {

		String[] beans = appContext.getBeanDefinitionNames();
        Arrays.sort(beans);
        for (String bean : beans) {
            System.out.println(bean);
        }

		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);
		// scheduledExecutorService.scheduleAtFixedRate(new
		// RemoveExpiredActiveTims(configuration, utility), 0, 4,
		// TimeUnit.HOURS);
		// scheduledExecutorService.scheduleAtFixedRate(new
		// CleanupActiveTims(configuration, utility), 0, 4,
		// TimeUnit.HOURS);

		sdxValidator.run();
	}
}