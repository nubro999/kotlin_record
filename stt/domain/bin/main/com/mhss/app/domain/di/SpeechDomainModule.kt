// com.mhss.app.domain.di 패키지에 생성
package com.mhss.app.domain.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.ksp.generated.module

@Module
@ComponentScan("com.mhss.app.domain.use_case")
class SpeechDomainModule

val speechDomainModule = org.koin.dsl.module {
    includes(SpeechDomainModule().module)
}
