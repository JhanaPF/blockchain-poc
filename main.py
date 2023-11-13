import hashlib

class CoinBlock:
    def __init__(self, previous_block_hash, transaction_list) -> None:
        self.previous_block_hash = previous_block_hash
        self.transaction_list = transaction_list

        self.block_data = "_".join(transaction_list) + "_" + previous_block_hash
        self.block_hash = hashlib.sha256(self.block_data.encode()).hexdigest()

t1 = "John sends 3 coins to Alfred"
t2 = "Michele sends 2.5 coins to Alfred"
t3 = "Alfred sends 6 coins to John"

initial_block = CoinBlock("Initial String", [t1, t2])

print(initial_block.block_data)
print(initial_block.block_hash)

second_block = CoinBlock(initial_block, [t3])

print(second_block.block_data)
print(second_block.block_hash)
