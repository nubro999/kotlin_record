// com.mhss.app.data.di 패키지에 생성/수정
package com.mhss.app.data.di

import com.mhss.app.domain.di.SpeechDomainModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.dsl.module
import org.koin.ksp.generated.module

@Module
@ComponentScan("com.mhss.app.data")
internal class SpeechDataModule

val speechDataModule = module {
    includes(SpeechDataModule().module, SpeechDomainModule().module)
}
