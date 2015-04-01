alter table laadproces add column automatisch_proces int8;

    alter table laadproces
        add constraint FK8C420DCE3DA16A8
        foreign key (automatisch_proces)
        references automatisch_proces;
