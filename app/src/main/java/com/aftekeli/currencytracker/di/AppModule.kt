package com.aftekeli.currencytracker.di

import com.aftekeli.currencytracker.data.repository.AuthRepository
import com.aftekeli.currencytracker.data.repository.AuthRepositoryImpl
import com.aftekeli.currencytracker.data.repository.CoinRepository
import com.aftekeli.currencytracker.data.repository.CoinRepositoryImpl
import com.aftekeli.currencytracker.data.repository.CommentRepository
import com.aftekeli.currencytracker.data.repository.CommentRepositoryImpl
import com.aftekeli.currencytracker.data.repository.PortfolioRepository
import com.aftekeli.currencytracker.data.repository.PortfolioRepositoryImpl
import com.aftekeli.currencytracker.data.repository.TransactionRepository
import com.aftekeli.currencytracker.data.repository.TransactionRepositoryImpl
import com.aftekeli.currencytracker.data.repository.UserRepository
import com.aftekeli.currencytracker.data.repository.UserRepositoryImpl
import com.aftekeli.currencytracker.data.repository.WalletRepository
import com.aftekeli.currencytracker.data.repository.WalletRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
    
    @Binds
    @Singleton
    abstract fun bindCoinRepository(
        coinRepositoryImpl: CoinRepositoryImpl
    ): CoinRepository
    
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
    
    @Binds
    @Singleton
    abstract fun bindCommentRepository(
        commentRepositoryImpl: CommentRepositoryImpl
    ): CommentRepository
    
    @Binds
    @Singleton
    abstract fun bindWalletRepository(impl: WalletRepositoryImpl): WalletRepository
    
    @Binds
    @Singleton
    abstract fun bindPortfolioRepository(impl: PortfolioRepositoryImpl): PortfolioRepository
    
    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore
} 