package effect.domain

interface EffectRepository {
    fun create(effect: Effect)
    fun searchById(id: Effect.Dto.Id): Effect?
}
