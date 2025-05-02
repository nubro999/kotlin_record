package org.koin.ksp.generated

import org.koin.core.module.Module
import org.koin.dsl.*


public val com_mhss_app_domain_di_SpeechDomainModule : Module get() = module {
	single() { _ -> com.mhss.app.domain.use_case.StartSpeechRecognitionUseCase(speechToTextRepository=get()) } 
	single() { _ -> com.mhss.app.domain.use_case.StopSpeechRecognitionUseCase(speechToTextRepository=get()) } 
}
public val com.mhss.app.domain.di.SpeechDomainModule.module : org.koin.core.module.Module get() = com_mhss_app_domain_di_SpeechDomainModule
