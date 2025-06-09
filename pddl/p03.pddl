(define (problem move-robot-3)
  (:domain robot-domain)
  (:objects r1 - robot l1 l2 - location)

  (:init
    (at r1 l2)
    (connected l2 l1)
  )

  (:goal (at r1 l1))
)
