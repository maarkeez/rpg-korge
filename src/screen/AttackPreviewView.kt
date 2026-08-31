package screen

import battleunit.domain.*
import korlibs.korge.ui.*
import korlibs.korge.view.*
import korlibs.math.geom.*
import unit.domain.*

class AttackPreviewView: Container() {

    private lateinit var casterUnitNameView: UnitNameView
    private lateinit var casterUnitPortraitView: UnitPortraitView
    private lateinit var casterHealthBarView: HealthBarView
    private lateinit var casterManaBarView: ManaBarView

    private lateinit var receiverUnitNameView: UnitNameView
    private lateinit var receiverUnitPortraitView: UnitPortraitView
    private lateinit var receiverHealthBarView: HealthBarView
    private lateinit var receiverManaBarView: ManaBarView

    private lateinit var delegate: Delegate

    suspend fun loadAssets() {
        casterUnitPortraitView.loadAssets()
        receiverUnitPortraitView.loadAssets()
    }

    private val layout = uiVerticalStack(padding = 5.0) {
            uiHorizontalStack {
                casterUnitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5))
                addChild(casterUnitPortraitView)

                uiSpacing(Size(10, 0))
                uiVerticalStack(padding = 1.0) {
                    casterUnitNameView = UnitNameView(size = Size(width = 281.5, height = 14))
                    addChild(casterUnitNameView)

                    uiSpacing(Size(0, 15))
                    casterHealthBarView =  HealthBarView(Size(281.5, 16))
                    addChild(casterHealthBarView)

                    uiSpacing(Size(0, 4))
                    casterManaBarView = ManaBarView(Size(281.5, 16))
                    addChild(casterManaBarView)
                }
            }
            uiSpacing(Size(0, 10))
            uiHorizontalStack {
                uiVerticalStack(padding = 1.0) {
                    receiverUnitNameView = UnitNameView(size = Size(width = 281.5, height = 14))
                    addChild(receiverUnitNameView)

                    uiSpacing(Size(0, 15))
                    receiverHealthBarView =  HealthBarView(Size(281.5, 16))
                    addChild(receiverHealthBarView)

                    uiSpacing(Size(0, 4))
                    receiverManaBarView = ManaBarView(Size(281.5, 16))
                    addChild(receiverManaBarView)
                }
                uiSpacing(Size(10, 0))
                receiverUnitPortraitView = UnitPortraitView(Size(width = 97.5, height = 97.5))
                addChild(receiverUnitPortraitView)

            }
        }


    init {
        visible = false
        addChild(layout)
    }

    fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    fun display(
        casterBattleUnit: BattleUnit.Dto,
        casterUnit: Unit.Dto,
        receiverBattleUnit: BattleUnit.Dto,
        receiverUnit: Unit.Dto,
    ) {
        casterUnitPortraitView.display(casterBattleUnit.unitId)
        casterUnitNameView.display(unitName = casterUnit.name)
        casterHealthBarView.display(
            remaining = casterBattleUnit.remainingHealthPoints,
            maximum = casterUnit.healthPoints,
        )
        casterManaBarView.display(
            remaining = casterBattleUnit.remainingManaPoints,
            maximum = casterUnit.manaPoints,
        )

        receiverUnitPortraitView.display(receiverBattleUnit.unitId)
        receiverUnitNameView.display(unitName = receiverUnit.name)
        receiverHealthBarView.display(
            remaining = receiverBattleUnit.remainingHealthPoints,
            maximum = receiverUnit.healthPoints,
        )
        receiverManaBarView.display(
            remaining = receiverBattleUnit.remainingManaPoints,
            maximum = receiverUnit.manaPoints,
        )

        visible = true
    }

    fun hide() {
        visible = false
    }

    interface Delegate {
        fun attackConfirmed()
        fun attackCancelled()
    }
}
