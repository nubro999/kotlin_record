package org.koin.ksp.generated

import org.koin.core.module.Module
import org.koin.dsl.*


internal val com_mhss_app_data_di_SpeechDataModule : Module get() = module {
	single() { _ -> com.mhss.app.data.SpeechToTextRepositoryImpl(context=get()) } bind(com.mhss.app.domain.repository.SpeechToTextRepository::class)
}
internal val com.mhss.app.data.di.SpeechDataModule.module : org.koin.core.module.Module get() = com_mhss_app_data_di_SpeechDataModule
