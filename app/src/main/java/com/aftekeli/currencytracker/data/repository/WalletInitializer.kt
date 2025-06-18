package com.aftekeli.currencytracker.data.repository

import com.aftekeli.currencytracker.data.model.VirtualWallet
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Kullanıcı cüzdanının başlatılması ve kontrolünü yöneten sınıf.
 * Uygulama başlatıldığında çalışır ve kullanıcının cüzdanını kontrol eder.
 */
@Singleton
class WalletInitializer @Inject constructor(
    private val walletRepository: WalletRepository,
    private val auth: FirebaseAuth
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    /**
     * Kullanıcı oturum açtığında cüzdan kontrolü yapar ve gerekirse oluşturur.
     * Bu metod uygulama başlatıldığında çağrılmalıdır.
     */
    fun initialize() {
        // Kullanıcının oturum durumunu dinle
        auth.addAuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                checkAndCreateWalletIfNeeded(currentUser.uid)
            }
        }
        
        // Eğer kullanıcı zaten giriş yapmışsa, hemen cüzdan kontrolü yap
        val currentUser = auth.currentUser
        if (currentUser != null) {
            checkAndCreateWalletIfNeeded(currentUser.uid)
        }
    }
    
    private fun checkAndCreateWalletIfNeeded(userId: String) {
        scope.launch {
            try {
                // Kullanıcının cüzdanını kontrol et
                val wallet = walletRepository.getUserWallet(userId).first()
                
                // Cüzdan yoksa oluştur
                if (wallet == null) {
                    val newWallet = VirtualWallet(
                        userId = userId,
                        balance = 1000.0, // Başlangıç bakiyesi
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    walletRepository.createOrUpdateWallet(newWallet)
                }
            } catch (e: Exception) {
                // Hata durumunda log kaydı tutulabilir
                e.printStackTrace()
            }
        }
    }
} 