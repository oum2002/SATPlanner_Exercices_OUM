(define (domain robot)
  (:predicates (at ?r))
  (:action move
    :parameters (?from ?to)
    :precondition (at ?from)
    :effect (and (not (at ?from)) (at ?to))
  )
)

