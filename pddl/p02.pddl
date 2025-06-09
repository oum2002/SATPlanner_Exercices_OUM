(define (problem move-robot-2)
  (:domain robot-domain)
  (:objects r1 - robot l1 l2 l3 l4 - location)

  (:init
    (at r1 l4)
    (connected l4 l1)
    (connected l1 l2)
    (connected l2 l3)
  )

  (:goal (at r1 l2))
)
