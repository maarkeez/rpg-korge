package battlefield.domain

interface BattlefieldRepository {
    fun create(battlefield: Battlefield)
    fun update(battlefield: Battlefield)
    fun search(): Battlefield?
}
