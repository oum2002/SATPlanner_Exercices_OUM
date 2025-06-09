(define (problem move-robot-1)
  (:domain robot-domain)
  (:objects r1 - robot l1 l2 l3 - location)

  (:init
    (at r1 l1)
    (connected l1 l2)
    (connected l2 l3)
    (connected l3 l1)
  )

  (:goal (at r1 l3))
)
