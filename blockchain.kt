import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.Signature
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*

data class Transaction(val amount: Double, val payer: String, val payee: String) {
    fun toStringValue(): String {
        return "$amount:$payer->$payee"
    }
}

data class Block(val prevHash: String, val transaction: Transaction, val ts: Long = System.currentTimeMillis()) {
    val nonce: Int = Random().nextInt(999999999)

    val hash: String
        get() {
            val str = toString()
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(str.toByteArray())
            return hash.joinToString("") { "%02x".format(it) }
        }
}

class Chain private constructor() {
    companion object {
        val instance = Chain()
    }

    val chain = mutableListOf<Block>()

    val lastBlock: Block
        get() = chain.last()

    fun mine(nonce: Int): Int {
        var solution = 1
        println("⛏️  mining...")

        while (true) {
            val hash = MessageDigest.getInstance("MD5")
            hash.update((nonce + solution).toString().toByteArray())

            val attempt = hash.digest().joinToString("") { "%02x".format(it) }

            if (attempt.startsWith("0000")) {
                println("Solved: $solution")
                return solution
            }

            solution++
        }
    }

    fun addBlock(transaction: Transaction, senderPublicKey: RSAPublicKey, signature: ByteArray) {
        val verify = Signature.getInstance("SHA256withRSA")
        verify.initVerify(senderPublicKey)
        verify.update(transaction.toStringValue().toByteArray())

        val isValid = verify.verify(signature)

        if (isValid) {
            val newBlock = Block(lastBlock.hash, transaction)
            mine(newBlock.nonce)
            chain.add(newBlock)
        }
    }
}

class Wallet {
    val publicKey: RSAPublicKey
    val privateKey: RSAPrivateKey

    init {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(2048)
        val keyPair = keyGen.generateKeyPair()

        publicKey = keyPair.public as RSAPublicKey
        privateKey = keyPair.private as RSAPrivateKey
    }

    fun sendMoney(amount: Double, payeePublicKey: RSAPublicKey) {
        val transaction = Transaction(amount, publicKey.toString(), payeePublicKey.toString())

        val sign = Signature.getInstance("SHA256withRSA")
        sign.initSign(privateKey)
        sign.update(transaction.toStringValue().toByteArray())
        val signature = sign.sign()

        Chain.instance.addBlock(transaction, publicKey, signature)
    }
}

fun main() {
    val satoshi = Wallet()
    val bob = Wallet()
    val alice = Wallet()

    satoshi.sendMoney(50.0, bob.publicKey)
    bob.sendMoney(23.0, alice.publicKey)
    alice.sendMoney(5.0, bob.publicKey)

    println(Chain.instance.chain)
}
