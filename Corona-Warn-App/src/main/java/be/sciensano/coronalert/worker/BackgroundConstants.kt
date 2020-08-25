package be.sciensano.coronalert.worker

import java.util.concurrent.TimeUnit

/**
 * The background work constants are used inside the BackgroundWorkScheduler
 *
 * @see BackgroundWorkScheduler
 */
object BackgroundConstants {
    
    /**
     * The minimum time in hours to wait between playbook executions
     *
     * @see TimeUnit.HOURS
     */
    const val MIN_HOURS_TO_NEXT_BACKGROUND_NOISE_EXECUTION = 24L

    /**
     * The maximum time in hours to wait between playbook executions
     *
     * @see TimeUnit.HOURS
     */
    const val MAX_HOURS_TO_NEXT_BACKGROUND_NOISE_EXECUTION = 24L
}
