-- =============================================================
--  LearnLink - Fake Seed Data for Testing
--  Generated: 2026-03-24
--  Run this AFTER the application has started once
--  (so Hibernate creates all tables via ddl-auto=update)
-- =============================================================

-- Disable FK checks temporarily so order doesn't matter
SET session_replication_role = replica;

-- =============================================================
-- 1. USERS  (passwords are BCrypt of "Password123!")
-- =============================================================
INSERT INTO users (id, email, username, password, active, email_verified, verification_code, role, "createdAt") VALUES
(1,  'admin@learnlink.ma',      'admin_learnlink',  '$2a$10$N.zmdr9k7uOCm0G45haqWOHnCT5yAphCepRKzT.5b8pYNNsNdLeq6', true,  true,  NULL, 'ADMIN',     NOW() - INTERVAL '90 days'),
(2,  'mod.sara@learnlink.ma',   'sara_mod',         '$2a$10$N.zmdr9k7uOCm0G45haqWOHnCT5yAphCepRKzT.5b8pYNNsNdLeq6', true,  true,  NULL, 'MODERATOR', NOW() - INTERVAL '60 days'),
(3,  'youssef.elkbiri@gmail.com','youssef_k',       '$2a$10$N.zmdr9k7uOCm0G45haqWOHnCT5yAphCepRKzT.5b8pYNNsNdLeq6', true,  true,  NULL, 'STUDENT',   NOW() - INTERVAL '55 days'),
(4,  'amina.benali@gmail.com',  'amina_b',          '$2a$10$N.zmdr9k7uOCm0G45haqWOHnCT5yAphCepRKzT.5b8pYNNsNdLeq6', true,  true,  NULL, 'STUDENT',   NOW() - INTERVAL '50 days'),
(5,  'omar.lahlou@gmail.com',   'omar_dev',         '$2a$10$N.zmdr9k7uOCm0G45haqWOHnCT5yAphCepRKzT.5b8pYNNsNdLeq6', true,  true,  NULL, 'STUDENT',   NOW() - INTERVAL '45 days'),
(6,  'fatima.zhra@gmail.com',   'fati_z',           '$2a$10$N.zmdr9k7uOCm0G45haqWOHnCT5yAphCepRKzT.5b8pYNNsNdLeq6', true,  true,  NULL, 'STUDENT',   NOW() - INTERVAL '40 days'),
(7,  'karim.tazi@gmail.com',    'karim_t',          '$2a$10$N.zmdr9k7uOCm0G45haqWOHnCT5yAphCepRKzT.5b8pYNNsNdLeq6', true,  true,  NULL, 'STUDENT',   NOW() - INTERVAL '35 days'),
(8,  'nadia.chrif@gmail.com',   'nadia_c',          '$2a$10$N.zmdr9k7uOCm0G45haqWOHnCT5yAphCepRKzT.5b8pYNNsNdLeq6', true,  true,  NULL, 'STUDENT',   NOW() - INTERVAL '30 days'),
(9,  'hamza.ait@gmail.com',     'hamza_a',          '$2a$10$N.zmdr9k7uOCm0G45haqWOHnCT5yAphCepRKzT.5b8pYNNsNdLeq6', true,  true,  NULL, 'STUDENT',   NOW() - INTERVAL '25 days'),
(10, 'siham.berrada@gmail.com', 'siham_bb',         '$2a$10$N.zmdr9k7uOCm0G45haqWOHnCT5yAphCepRKzT.5b8pYNNsNdLeq6', true,  true,  NULL, 'STUDENT',   NOW() - INTERVAL '20 days'),
(11, 'zakaria.mf@gmail.com',    'zakaria_m',        '$2a$10$N.zmdr9k7uOCm0G45haqWOHnCT5yAphCepRKzT.5b8pYNNsNdLeq6', true,  false, 'VER123CODE', 'STUDENT',  NOW() - INTERVAL '5 days'),
(12, 'support.learnlink@gmail.com','support_team',  '$2a$10$N.zmdr9k7uOCm0G45haqWOHnCT5yAphCepRKzT.5b8pYNNsNdLeq6', true,  true,  NULL, 'SUPPORT',   NOW() - INTERVAL '80 days');

-- Reset sequence
SELECT setval(pg_get_serial_sequence('users', 'id'), 12);

-- =============================================================
-- 2. STUDENT SUBJECTS
-- =============================================================
INSERT INTO student_subject (id, name) VALUES
(1,  'Mathématiques'),
(2,  'Physique'),
(3,  'Chimie'),
(4,  'Biologie'),
(5,  'Informatique'),
(6,  'Algorithmique'),
(7,  'Structures de Données'),
(8,  'Base de Données'),
(9,  'Développement Web'),
(10, 'Intelligence Artificielle'),
(11, 'Réseaux Informatiques'),
(12, 'Systèmes d''Exploitation'),
(13, 'Littérature Française'),
(14, 'Langue Anglaise'),
(15, 'Économie'),
(16, 'Histoire'),
(17, 'Géographie'),
(18, 'Philosophie'),
(19, 'Statistiques'),
(20, 'Électronique');

SELECT setval(pg_get_serial_sequence('student_subject', 'id'), 20);

-- =============================================================
-- 3. USER PROFILES
-- =============================================================
INSERT INTO user_profiles (id, user_id, bio, first_name, last_name, profile_picture_url, academic_level) VALUES
(1,  3,  'Passionné de programmation et d''algorithmique. J''aime partager mes connaissances et apprendre avec d''autres étudiants.',
         'Youssef',  'Elkbiri',  NULL, 'BACHELOR'),
(2,  4,  'Étudiante en mathématiques appliquées. Je cherche des partenaires de révision sérieux.',
         'Amina',    'Benali',   NULL, 'BACHELOR'),
(3,  5,  'Développeur passionné, spécialisé en Java et Python. Toujours partant pour un projet collaboratif!',
         'Omar',     'Lahlou',   NULL, 'MASTER'),
(4,  6,  'Étudiante en chimie et biologie. J''aime expliquer les concepts difficiles avec des exemples du quotidien.',
         'Fatima',   'Zhra',     NULL, 'BACHELOR'),
(5,  7,  'Passionné par la physique et les mathématiques. Futur ingénieur en génie électrique.',
         'Karim',    'Tazi',     NULL, 'BACHELOR'),
(6,  8,  'Étudiante en biologie moléculaire. La recherche scientifique est ma passion.',
         'Nadia',    'Chrif',    NULL, 'MASTER'),
(7,  9,  'Étudiant en informatique avec un intérêt particulier pour les structures de données et l''IA.',
         'Hamza',    'Ait',      NULL, 'BACHELOR'),
(8,  10, 'Passionnée par la chimie organique et les sciences naturelles.',
         'Siham',    'Berrada',  NULL, 'HIGH_SCHOOL'),
(9,  11, 'Nouvel étudiant sur LearnLink. Intéressé par le développement web et les bases de données.',
         'Zakaria',  'Moufid',   NULL, 'BACHELOR'),
(10, 2,  'Modérateur de la communauté LearnLink. Ici pour assurer un environnement d''apprentissage sain.',
         'Sara',     'Mod',      NULL, 'MASTER'),
(11, 1,  'Administrateur de la plateforme LearnLink.',
         'Admin',    'LearnLink',NULL, 'OTHER');

SELECT setval(pg_get_serial_sequence('user_profiles', 'id'), 11);

-- =============================================================
-- 4. USER PROFILE <-> SUBJECT ASSOCIATIONS (join table)
-- =============================================================
INSERT INTO user_profile_subject (profile_id, subject_id) VALUES
-- Youssef (profile 1): Informatique, Algo, Structures, BDD, Web
(1, 5), (1, 6), (1, 7), (1, 8), (1, 9),
-- Amina (profile 2): Maths, Stats, Économie
(2, 1), (2, 19), (2, 15),
-- Omar (profile 3): Informatique, Java(Web), Python(Algo), BDD, IA
(3, 5), (3, 6), (3, 9), (3, 8), (3, 10),
-- Fatima (profile 4): Chimie, Biologie, Physique
(4, 3), (4, 4), (4, 2),
-- Karim (profile 5): Physique, Maths, Électronique, Informatique
(5, 2), (5, 1), (5, 20), (5, 5),
-- Nadia (profile 6): Biologie, Chimie, Stats
(6, 4), (6, 3), (6, 19),
-- Hamza (profile 7): Informatique, Algo, Structures, IA, Réseaux
(7, 5), (7, 6), (7, 7), (7, 10), (7, 11),
-- Siham (profile 8): Chimie, Biologie, Physique, Maths
(8, 3), (8, 4), (8, 2), (8, 1),
-- Zakaria (profile 9): Web, BDD, Informatique
(9, 9), (9, 8), (9, 5);

-- =============================================================
-- 5. MODERATOR PERMISSIONS
-- =============================================================
INSERT INTO moderator_permissions (id, user_id, assigned_by, assigned_at, updated_at, notes) VALUES
(1, 2, 1, NOW() - INTERVAL '60 days', NOW() - INTERVAL '10 days', 'Primary content moderator for Arabic/French posts');

INSERT INTO moderator_permission_list (moderator_permission_id, permission) VALUES
(1, 'HIDE_POSTS'),
(1, 'HIDE_COMMENTS'),
(1, 'HIDE_QUESTIONS'),
(1, 'HIDE_ANSWERS'),
(1, 'VIEW_USER_DETAILS'),
(1, 'WARN_USERS'),
(1, 'VIEW_REPORTS'),
(1, 'RESOLVE_REPORTS');

SELECT setval(pg_get_serial_sequence('moderator_permissions', 'id'), 1);

-- =============================================================
-- 6. CONNECTION REQUESTS
-- =============================================================
INSERT INTO connection_requests (id, sender_id, receiver_id, message, status, compatibility_score, created_at, updated_at) VALUES
(1,  3, 4,  'Salut Amina! Je pense qu''on peut s''aider mutuellement en maths.',   'ACCEPTED',  87.50, NOW() - INTERVAL '48 days', NOW() - INTERVAL '47 days'),
(2,  3, 5,  'Hey Omar, on peut pratiquer le code ensemble?',                         'ACCEPTED',  92.00, NOW() - INTERVAL '45 days', NOW() - INTERVAL '44 days'),
(3,  4, 6,  'Salut Fatima! tu fais quelle filière?',                                 'ACCEPTED',  78.25, NOW() - INTERVAL '38 days', NOW() - INTERVAL '37 days'),
(4,  5, 7,  'Karim, on devrait partager des ressources Python!',                     'ACCEPTED',  95.00, NOW() - INTERVAL '33 days', NOW() - INTERVAL '32 days'),
(5,  6, 8,  'Nadia, tu es en biologie aussi?',                                       'ACCEPTED',  81.75, NOW() - INTERVAL '28 days', NOW() - INTERVAL '27 days'),
(6,  7, 9,  'Hamza, j''ai vu ton post sur les structures de données, excellent!',   'ACCEPTED',  89.00, NOW() - INTERVAL '23 days', NOW() - INTERVAL '22 days'),
(7,  8, 10, 'Siham, tu peux m''aider avec la chimie organique?',                    'ACCEPTED',  74.50, NOW() - INTERVAL '18 days', NOW() - INTERVAL '17 days'),
(8,  9, 11, 'Zakaria, bienvenue sur LearnLink!',                                     'PENDING',   65.00, NOW() - INTERVAL '3 days',  NOW() - INTERVAL '3 days'),
(9,  10, 3, 'Youssef, tes posts sur l''algo sont super!',                           'PENDING',   70.25, NOW() - INTERVAL '2 days',  NOW() - INTERVAL '2 days'),
(10, 4, 7,  'Karim, on peut étudier ensemble les mathématiques?',                   'REJECTED',  55.00, NOW() - INTERVAL '40 days', NOW() - INTERVAL '39 days');

SELECT setval(pg_get_serial_sequence('connection_requests', 'id'), 10);

-- =============================================================
-- 7. CONNECTIONS (accepted requests become connections)
-- =============================================================
INSERT INTO connections (id, user1_id, user2_id, status, compatibility_score, connected_at) VALUES
(1, 3, 4,  'ACTIVE', 87.50, NOW() - INTERVAL '47 days'),
(2, 3, 5,  'ACTIVE', 92.00, NOW() - INTERVAL '44 days'),
(3, 4, 6,  'ACTIVE', 78.25, NOW() - INTERVAL '37 days'),
(4, 5, 7,  'ACTIVE', 95.00, NOW() - INTERVAL '32 days'),
(5, 6, 8,  'ACTIVE', 81.75, NOW() - INTERVAL '27 days'),
(6, 7, 9,  'ACTIVE', 89.00, NOW() - INTERVAL '22 days'),
(7, 8, 10, 'ACTIVE', 74.50, NOW() - INTERVAL '17 days');

SELECT setval(pg_get_serial_sequence('connections', 'id'), 7);

-- =============================================================
-- 8. COMMUNITY POSTS
-- =============================================================
INSERT INTO community_posts (id, user_id, title, content, type, category, view_count, likes_count, comments_count, created_at, updated_at, hidden, hidden_at, hidden_by, hidden_reason) VALUES
(1,  3, 'Résumé complet : Algorithmes de tri en Python',
    'Voici un résumé des principaux algorithmes de tri : Bubble Sort, Merge Sort, Quick Sort, et Heap Sort. Chacun a sa propre complexité temporelle...',
    'SUMMARY', 'PROGRAMMING', 245, 18, 5, NOW() - INTERVAL '40 days', NOW() - INTERVAL '40 days', false, NULL, NULL, NULL),

(2,  5, 'Tutoriel: Comprendre les pointeurs en C',
    'Les pointeurs sont l''un des concepts les plus difficiles à comprendre en C. Dans ce tutoriel, nous allons les démystifier étape par étape...',
    'TUTORIAL', 'PROGRAMMING', 312, 24, 8, NOW() - INTERVAL '35 days', NOW() - INTERVAL '35 days', false, NULL, NULL, NULL),

(3,  4, 'Discussion: Quelle est la meilleure méthode pour apprendre les intégrales?',
    'Je bloque sur les intégrales depuis 2 semaines. Est-ce que quelqu''un peut me recommander une méthode ou une ressource utile?',
    'DISCUSSION', 'MATHEMATICS', 189, 12, 10, NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days', false, NULL, NULL, NULL),

(4,  7, 'Résumé: Les lois de Newton expliquées simplement',
    'Les trois lois de Newton sont fondamentales en physique. Première loi (inertie) : Tout corps persiste dans son état de repos ou de mouvement...',
    'SUMMARY', 'PHYSICS', 156, 9, 3, NOW() - INTERVAL '28 days', NOW() - INTERVAL '28 days', false, NULL, NULL, NULL),

(5,  8, 'Tutoriel: Comprendre la photosynthèse',
    'La photosynthèse est le processus par lequel les plantes convertissent la lumière solaire en énergie chimique. Voici une explication détaillée...',
    'TUTORIAL', 'BIOLOGY', 203, 15, 6, NOW() - INTERVAL '25 days', NOW() - INTERVAL '25 days', false, NULL, NULL, NULL),

(6,  6, 'Discussion: Conseils pour réussir les examens de chimie?',
    'Les examens de chimie approchent et je me sens pas prête. Est-ce que vous avez des conseils ou des stratégies de révision?',
    'DISCUSSION', 'CHEMISTRY', 134, 7, 7, NOW() - INTERVAL '22 days', NOW() - INTERVAL '22 days', false, NULL, NULL, NULL),

(7,  9, 'Résumé: Introduction à la complexité algorithmique Big-O',
    'La notation Big-O est essentielle pour évaluer la performance des algorithmes. O(1), O(n), O(n log n), O(n²)...',
    'SUMMARY', 'PROGRAMMING', 278, 21, 4, NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days', false, NULL, NULL, NULL),

(8,  10, 'Discussion: Comment mémoriser les formules de chimie?',
    'J''ai du mal à mémoriser toutes les formules chimiques. Des astuces mnémotechniques peut-être?',
    'DISCUSSION', 'CHEMISTRY', 98,  6,  4, NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days', false, NULL, NULL, NULL),

(9,  3, 'Tutoriel: SQL pour débutants - les JOINs expliqués',
    'Les JOINs en SQL permettent de combiner des données de plusieurs tables. INNER JOIN, LEFT JOIN, RIGHT JOIN, FULL OUTER JOIN...',
    'TUTORIAL', 'PROGRAMMING', 421, 32, 9, NOW() - INTERVAL '12 days', NOW() - INTERVAL '12 days', false, NULL, NULL, NULL),

(10, 5, 'Post signalé - contenu inapproprié (masqué)',
    'Ce contenu a été masqué par un modérateur.',
    'DISCUSSION', 'OTHER', 22,  0,  0, NOW() - INTERVAL '10 days', NOW() - INTERVAL '9 days', true, NOW() - INTERVAL '9 days', 2, 'Contenu offensant signalé par plusieurs utilisateurs');

SELECT setval(pg_get_serial_sequence('community_posts', 'id'), 10);

-- =============================================================
-- 9. COMMUNITY QUESTIONS
-- =============================================================
INSERT INTO community_questions (id, user_id, title, content, view_count, is_resolved, accepted_answer_id, created_at, updated_at, hidden, hidden_at, hidden_by, hidden_reason) VALUES
(1, 4,  'Comment résoudre une équation différentielle du second ordre?',
   'J''essaie de résoudre y'' + 3y'' + 2y = 0 mais je n''arrive pas à trouver la méthode correcte. Pouvez-vous m''aider?',
   310, true, 2, NOW() - INTERVAL '38 days', NOW() - INTERVAL '37 days', false, NULL, NULL, NULL),

(2, 6,  'Quelle est la différence entre l''oxydation et la réduction en chimie?',
   'Je confonds toujours oxydation et réduction dans les réactions redox. Une explication claire serait appréciée.',
   198, true, 4, NOW() - INTERVAL '32 days', NOW() - INTERVAL '31 days', false, NULL, NULL, NULL),

(3, 8,  'Comment fonctionne la division cellulaire (mitose vs méiose)?',
   'Je dois présenter la division cellulaire demain mais je ne comprends pas bien la différence entre mitose et méiose.',
   245, false, NULL, NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days', false, NULL, NULL, NULL),

(4, 10, 'Pourquoi Python utilise des indentations au lieu des accolades?',
   'En venant du Java, je trouve les indentations en Python déroutantes. Quel est l''intérêt de ce choix de design?',
   387, true, 7, NOW() - INTERVAL '15 days', NOW() - INTERVAL '14 days', false, NULL, NULL, NULL),

(5, 7,  'Comment calculer le moment d''une force en physique?',
   'J''ai un exercice sur les moments de force et je ne sais pas par où commencer. Quelqu''un peut expliquer?',
   156, false, NULL, NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days', false, NULL, NULL, NULL);

SELECT setval(pg_get_serial_sequence('community_questions', 'id'), 5);

-- =============================================================
-- 10. COMMUNITY ANSWERS
-- =============================================================
INSERT INTO community_answers (id, question_id, user_id, content, vote_count, upvote_count, downvote_count, is_accepted, created_at, updated_at, hidden, hidden_at, hidden_by, hidden_reason) VALUES
(1, 1, 3, 'Pour résoudre y'''' + 3y'' + 2y = 0, on pose r² + 3r + 2 = 0 (équation caractéristique). On factorise: (r+1)(r+2)=0 donc r1=-1 et r2=-2. La solution générale est y = C1·e^(-x) + C2·e^(-2x).',
   8, 9, 1, false, NOW() - INTERVAL '37 days', NOW() - INTERVAL '37 days', false, NULL, NULL, NULL),

(2, 1, 5, 'Excellente réponse au-dessus! Pour compléter: si l''équation était non-homogène (avec un membre de droite), il faudrait trouver une solution particulière par la méthode des coefficients indéterminés ou la variation des constantes.',
   12, 13, 1, true, NOW() - INTERVAL '37 days', NOW() - INTERVAL '36 days', false, NULL, NULL, NULL),

(3, 2, 9, 'L''oxydation c''est la perte d''électrons (OIL: Oxidation Is Loss) et la réduction c''est le gain d''électrons (RIG: Reduction Is Gain). Mémo: OIL RIG. Dans une réaction redox, le réducteur se fait oxyder et l''oxydant se fait réduire.',
   6, 7, 1, false, NOW() - INTERVAL '31 days', NOW() - INTERVAL '31 days', false, NULL, NULL, NULL),

(4, 2, 3, 'Pour aller plus loin: le nombre d''oxydation est l''outil clé. Si le nombre d''oxydation augmente → oxydation. S''il diminue → réduction. Exemple: Fe → Fe²⁺ (oxydation, perd 2 électrons), Cu²⁺ → Cu (réduction, gagne 2 électrons).',
   15, 16, 1, true, NOW() - INTERVAL '31 days', NOW() - INTERVAL '30 days', false, NULL, NULL, NULL),

(5, 3, 8, 'La mitose produit 2 cellules filles génétiquement identiques à la cellule mère (2n chromosomes). La méiose produit 4 cellules filles haploïdes (n chromosomes) avec brassage génétique. La mitose sert pour la croissance, la méiose pour la reproduction sexuée.',
   5, 5, 0, false, NOW() - INTERVAL '19 days', NOW() - INTERVAL '19 days', false, NULL, NULL, NULL),

(6, 3, 4, 'Pour retenir facilement: mitose = même nombre de chromosomes = croissance. Méiose = moitié des chromosomes = reproduction. Les phases: Prophase, Métaphase, Anaphase, Télophase (PMAT) sont communes aux deux, mais la méiose en a 2 cycles.',
   3, 3, 0, false, NOW() - INTERVAL '19 days', NOW() - INTERVAL '19 days', false, NULL, NULL, NULL),

(7, 4, 5, 'Guido van Rossum (créateur de Python) a choisi les indentations pour forcer la lisibilité du code. Le but est que tout le monde écrive du code lisible de la même manière. C''est une décision délibérée: le code Python lisible ressemble presque à du pseudo-code.',
   18, 19, 1, true, NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days', false, NULL, NULL, NULL),

(8, 5, 3, 'Le moment d''une force M = F × d, où F est la force en Newtons et d est le bras de levier (distance perpendiculaire en mètres). L''unité est le Newton·mètre (N·m). Exemple: une force de 10N à 0.5m du pivot donne M = 5 N·m.',
   4, 4, 0, false, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days', false, NULL, NULL, NULL);

-- Update accepted_answer_id references
UPDATE community_questions SET accepted_answer_id = 2 WHERE id = 1;
UPDATE community_questions SET accepted_answer_id = 4 WHERE id = 2;
UPDATE community_questions SET accepted_answer_id = 7 WHERE id = 4;

SELECT setval(pg_get_serial_sequence('community_answers', 'id'), 8);

-- =============================================================
-- 11. COMMUNITY COMMENTS
-- =============================================================
INSERT INTO community_comments (id, post_id, answer_id, user_id, content, likes_count, created_at, updated_at, hidden, hidden_at, hidden_by, hidden_reason) VALUES
-- Comments on posts
(1,  1, NULL, 4,  'Super résumé! Tu devrais ajouter le Radix Sort aussi.', 3, NOW() - INTERVAL '39 days', NOW() - INTERVAL '39 days', false, NULL, NULL, NULL),
(2,  1, NULL, 5,  'Merci pour ce résumé, très clair et bien structuré!',   2, NOW() - INTERVAL '39 days', NOW() - INTERVAL '39 days', false, NULL, NULL, NULL),
(3,  2, NULL, 3,  'Ce tutoriel m''a enfin fait comprendre les pointeurs!', 5, NOW() - INTERVAL '34 days', NOW() - INTERVAL '34 days', false, NULL, NULL, NULL),
(4,  2, NULL, 9,  'Est-ce que tu peux ajouter un exemple avec les pointeurs de pointeurs?', 1, NOW() - INTERVAL '34 days', NOW() - INTERVAL '34 days', false, NULL, NULL, NULL),
(5,  3, NULL, 7,  'Personnellement j''utilise Khan Academy pour les intégrales, très bien expliqué.',  2, NOW() - INTERVAL '29 days', NOW() - INTERVAL '29 days', false, NULL, NULL, NULL),
(6,  3, NULL, 5,  'Essaie aussi 3Blue1Brown sur YouTube, ses animations rendent les maths visuellement intuitives.', 4, NOW() - INTERVAL '29 days', NOW() - INTERVAL '29 days', false, NULL, NULL, NULL),
(7,  7, NULL, 4,  'Excellent! Il manque la complexité spatiale mais sinon parfait.', 2, NOW() - INTERVAL '19 days', NOW() - INTERVAL '19 days', false, NULL, NULL, NULL),
(8,  9, NULL, 7,  'Les exemples sont très bien choisis. J''ai enfin compris le LEFT JOIN!', 6, NOW() - INTERVAL '11 days', NOW() - INTERVAL '11 days', false, NULL, NULL, NULL),
(9,  9, NULL, 8,  'Pourrais-tu ajouter un exemple avec CROSS JOIN?', 1, NOW() - INTERVAL '11 days', NOW() - INTERVAL '11 days', false, NULL, NULL, NULL),
-- Comments on answers
(10, NULL, 2, 6,  'Merci beaucoup! Cette explication m''a sauvé pour mon exam!', 3, NOW() - INTERVAL '36 days', NOW() - INTERVAL '36 days', false, NULL, NULL, NULL),
(11, NULL, 4, 6,  'OIL RIG, c''est le meilleur moyen mnémotechnique, merci!',   4, NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days', false, NULL, NULL, NULL),
(12, NULL, 7, 10, 'Très bonne explication! Python est vraiment élégant.',       2, NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days', false, NULL, NULL, NULL);

SELECT setval(pg_get_serial_sequence('community_comments', 'id'), 12);

-- =============================================================
-- 12. POST LIKES
-- =============================================================
INSERT INTO post_likes (post_id, user_id, created_at) VALUES
(1, 4, NOW() - INTERVAL '39 days'),
(1, 5, NOW() - INTERVAL '39 days'),
(1, 6, NOW() - INTERVAL '38 days'),
(2, 3, NOW() - INTERVAL '34 days'),
(2, 4, NOW() - INTERVAL '34 days'),
(2, 8, NOW() - INTERVAL '33 days'),
(3, 5, NOW() - INTERVAL '29 days'),
(3, 7, NOW() - INTERVAL '29 days'),
(5, 3, NOW() - INTERVAL '24 days'),
(5, 4, NOW() - INTERVAL '24 days'),
(7, 3, NOW() - INTERVAL '19 days'),
(7, 4, NOW() - INTERVAL '19 days'),
(7, 5, NOW() - INTERVAL '18 days'),
(9, 4, NOW() - INTERVAL '11 days'),
(9, 6, NOW() - INTERVAL '11 days'),
(9, 7, NOW() - INTERVAL '11 days'),
(9, 8, NOW() - INTERVAL '10 days');

-- =============================================================
-- 13. ANSWER VOTES
-- =============================================================
INSERT INTO answer_votes (answer_id, user_id, vote_type, created_at) VALUES
(1, 4, 'UPVOTE',   NOW() - INTERVAL '37 days'),
(1, 6, 'UPVOTE',   NOW() - INTERVAL '37 days'),
(1, 8, 'DOWNVOTE', NOW() - INTERVAL '37 days'),
(2, 4, 'UPVOTE',   NOW() - INTERVAL '36 days'),
(2, 6, 'UPVOTE',   NOW() - INTERVAL '36 days'),
(2, 7, 'UPVOTE',   NOW() - INTERVAL '36 days'),
(2, 9, 'DOWNVOTE', NOW() - INTERVAL '36 days'),
(4, 3, 'UPVOTE',   NOW() - INTERVAL '30 days'),
(4, 5, 'UPVOTE',   NOW() - INTERVAL '30 days'),
(4, 6, 'UPVOTE',   NOW() - INTERVAL '30 days'),
(4, 8, 'DOWNVOTE', NOW() - INTERVAL '30 days'),
(7, 4, 'UPVOTE',   NOW() - INTERVAL '14 days'),
(7, 6, 'UPVOTE',   NOW() - INTERVAL '14 days'),
(7, 8, 'UPVOTE',   NOW() - INTERVAL '13 days'),
(7, 9, 'DOWNVOTE', NOW() - INTERVAL '13 days');

-- =============================================================
-- 14. MESSAGES (Direct messages between connected users)
-- =============================================================
INSERT INTO messages (id, sender_id, recipient_id, content, message_type, status, attachment_key, attachment_name, read_at, created_at, updated_at) VALUES
-- Conversation: Youssef (3) ↔ Amina (4)
(1,  3, 4,  'Salam Amina! Comment tu vas?',                                             'TEXT', 'READ',      NULL, NULL, NOW() - INTERVAL '46 days 10h', NOW() - INTERVAL '46 days 12h', NOW() - INTERVAL '46 days 10h'),
(2,  4, 3,  'Salam Youssef! Ça va bien merci, et toi?',                                'TEXT', 'READ',      NULL, NULL, NOW() - INTERVAL '46 days 9h',  NOW() - INTERVAL '46 days 9h',  NOW() - INTERVAL '46 days 9h'),
(3,  3, 4,  'Ça va! Tu veux qu''on révise les maths ensemble ce weekend?',              'TEXT', 'READ',      NULL, NULL, NOW() - INTERVAL '46 days 8h',  NOW() - INTERVAL '46 days 8h',  NOW() - INTERVAL '46 days 8h'),
(4,  4, 3,  'Bonne idée! Samedi ça te va?',                                             'TEXT', 'READ',      NULL, NULL, NOW() - INTERVAL '46 days 7h',  NOW() - INTERVAL '46 days 7h',  NOW() - INTERVAL '46 days 7h'),
(5,  3, 4,  'Parfait, samedi à 14h!',                                                   'TEXT', 'DELIVERED', NULL, NULL, NULL,                            NOW() - INTERVAL '2 days',       NOW() - INTERVAL '2 days'),
-- Conversation: Omar (5) ↔ Karim (7)
(6,  5, 7,  'Karim, t''as regardé le cours sur les classes abstraites en Java?',        'TEXT', 'READ',      NULL, NULL, NOW() - INTERVAL '31 days 5h',  NOW() - INTERVAL '31 days 6h',  NOW() - INTERVAL '31 days 5h'),
(7,  7, 5,  'Oui! C''est super intéressant. Les interfaces vs classes abstraites...',   'TEXT', 'READ',      NULL, NULL, NOW() - INTERVAL '31 days 4h',  NOW() - INTERVAL '31 days 4h',  NOW() - INTERVAL '31 days 4h'),
(8,  5, 7,  'Exactement! Tu veux faire un projet ensemble pour pratiquer?',             'TEXT', 'READ',      NULL, NULL, NOW() - INTERVAL '31 days 3h',  NOW() - INTERVAL '31 days 3h',  NOW() - INTERVAL '31 days 3h'),
(9,  7, 5,  'Super idée! On peut faire une API REST simple avec Spring Boot.',          'TEXT', 'SENT',      NULL, NULL, NULL,                            NOW() - INTERVAL '1 day',        NOW() - INTERVAL '1 day'),
-- Conversation: Fatima (6) ↔ Nadia (8)
(10, 6, 8,  'Nadia, tu peux m''envoyer tes notes sur la biologie cellulaire?',         'TEXT', 'READ',      NULL, NULL, NOW() - INTERVAL '26 days 2h',  NOW() - INTERVAL '26 days 3h',  NOW() - INTERVAL '26 days 2h'),
(11, 8, 6,  'Bien sûr! Je te les envoie ce soir.',                                     'TEXT', 'READ',      NULL, NULL, NOW() - INTERVAL '26 days 1h',  NOW() - INTERVAL '26 days 1h',  NOW() - INTERVAL '26 days 1h'),
(12, 8, 6,  'Notes de biologie cellulaire - Chapitre 3',                               'FILE', 'READ',      'uploads/bio_notes_ch3.pdf', 'biologie_cellulaire_ch3.pdf', NOW() - INTERVAL '26 days', NOW() - INTERVAL '26 days', NOW() - INTERVAL '26 days');

SELECT setval(pg_get_serial_sequence('messages', 'id'), 12);

-- =============================================================
-- 15. BADGES
-- =============================================================
INSERT INTO badges (id, code, name, description, icon_url, type, rarity, points_required, active, created_at) VALUES
(1,  'FIRST_POST',        'Premier Post',           'A posté son premier contenu sur la communauté',                    '/icons/badges/first_post.svg',         'FIRST_POST',           'COMMON',    0,    true, NOW() - INTERVAL '90 days'),
(2,  'HELPFUL_10',        'Aide Précieuse',         'A reçu 10 upvotes sur ses réponses',                              '/icons/badges/helpful.svg',            'HELPFUL_CONTRIBUTOR',  'UNCOMMON',  0,    true, NOW() - INTERVAL '90 days'),
(3,  'EXPERT_PROG',       'Expert Programmation',   'Reconnu comme expert en programmation',                           '/icons/badges/expert_prog.svg',        'EXPERT',               'RARE',      500,  true, NOW() - INTERVAL '90 days'),
(4,  'STREAK_7',          'Warrior 7 Jours',        'Connecté 7 jours consécutifs',                                    '/icons/badges/streak7.svg',            'STREAK_WARRIOR',       'UNCOMMON',  0,    true, NOW() - INTERVAL '90 days'),
(5,  'LEVEL_5',           'Niveau 5',               'A atteint le niveau 5',                                           '/icons/badges/level5.svg',             'LEVEL_MASTER',         'RARE',      0,    true, NOW() - INTERVAL '90 days'),
(6,  'THOUSAND_PTS',      'Millionnaire de Points', 'A accumulé 1000 points',                                          '/icons/badges/1000pts.svg',            'THOUSAND_POINTS',      'EPIC',      1000, true, NOW() - INTERVAL '90 days'),
(7,  'CONNECTOR_5',       'Connecteur',             'A établi 5 connexions avec d''autres étudiants',                  '/icons/badges/connector.svg',          'CONNECTOR',            'COMMON',    0,    true, NOW() - INTERVAL '90 days'),
(8,  'MENTOR_BADGE',      'Mentor Communautaire',   'A eu 3 réponses acceptées comme solution',                        '/icons/badges/mentor.svg',             'MENTOR',               'EPIC',      0,    true, NOW() - INTERVAL '90 days'),
(9,  'COMMUNITY_LEADER',  'Leader Communautaire',   'A contribué de manière exceptionnelle à la communauté',           '/icons/badges/leader.svg',             'COMMUNITY_LEADER',     'LEGENDARY', 2000, true, NOW() - INTERVAL '90 days');

SELECT setval(pg_get_serial_sequence('badges', 'id'), 9);

-- =============================================================
-- 16. USER SCORES
-- =============================================================
INSERT INTO user_scores (id, user_id, total_points, level, current_level_points, points_for_next_level, created_at, updated_at) VALUES
(1,  3,  1450, 7, 50,  450, NOW() - INTERVAL '55 days', NOW() - INTERVAL '1 day'),
(2,  4,  890,  5, 90,  350, NOW() - INTERVAL '50 days', NOW() - INTERVAL '2 days'),
(3,  5,  1120, 6, 20,  400, NOW() - INTERVAL '45 days', NOW() - INTERVAL '1 day'),
(4,  6,  540,  4, 40,  300, NOW() - INTERVAL '40 days', NOW() - INTERVAL '3 days'),
(5,  7,  760,  5, 10,  350, NOW() - INTERVAL '35 days', NOW() - INTERVAL '2 days'),
(6,  8,  430,  3, 80,  250, NOW() - INTERVAL '30 days', NOW() - INTERVAL '4 days'),
(7,  9,  670,  4, 70,  300, NOW() - INTERVAL '25 days', NOW() - INTERVAL '3 days'),
(8,  10, 210,  2, 60,  200, NOW() - INTERVAL '20 days', NOW() - INTERVAL '5 days'),
(9,  11, 50,   1, 50,  100, NOW() - INTERVAL '5 days',  NOW() - INTERVAL '5 days');

SELECT setval(pg_get_serial_sequence('user_scores', 'id'), 9);

-- =============================================================
-- 17. USER BADGES (earned badges)
-- =============================================================
INSERT INTO user_badges (id, user_id, badge_id, earned_at) VALUES
(1,  3, 1, NOW() - INTERVAL '40 days'),  -- Youssef: Premier Post
(2,  3, 4, NOW() - INTERVAL '30 days'),  -- Youssef: Streak 7j
(3,  3, 3, NOW() - INTERVAL '20 days'),  -- Youssef: Expert Prog
(4,  3, 6, NOW() - INTERVAL '10 days'),  -- Youssef: 1000 Points
(5,  4, 1, NOW() - INTERVAL '30 days'),  -- Amina: Premier Post
(6,  4, 4, NOW() - INTERVAL '20 days'),  -- Amina: Streak 7j
(7,  5, 1, NOW() - INTERVAL '35 days'),  -- Omar: Premier Post
(8,  5, 2, NOW() - INTERVAL '14 days'),  -- Omar: Aide Précieuse
(9,  5, 8, NOW() - INTERVAL '7 days'),   -- Omar: Mentor
(10, 7, 1, NOW() - INTERVAL '28 days'),  -- Karim: Premier Post
(11, 7, 7, NOW() - INTERVAL '22 days'),  -- Karim: Connecteur
(12, 9, 1, NOW() - INTERVAL '25 days'),  -- Hamza: Premier Post
(13, 9, 2, NOW() - INTERVAL '10 days');  -- Hamza: Aide Précieuse

SELECT setval(pg_get_serial_sequence('user_badges', 'id'), 13);

-- =============================================================
-- 18. NOTIFICATIONS
-- =============================================================
INSERT INTO notifications (id, user_id, type, title, message, data, is_read, read_at, created_at) VALUES
(1,  3,  'CONNECTION_ACCEPTED', 'Connexion acceptée',   'Amina Benali a accepté votre demande de connexion.',      '{"userId": 4, "username": "amina_b"}',               true,  NOW() - INTERVAL '47 days', NOW() - INTERVAL '47 days'),
(2,  3,  'CONNECTION_ACCEPTED', 'Connexion acceptée',   'Omar Lahlou a accepté votre demande de connexion.',       '{"userId": 5, "username": "omar_dev"}',               true,  NOW() - INTERVAL '44 days', NOW() - INTERVAL '44 days'),
(3,  4,  'CONNECTION_REQUEST',  'Nouvelle demande',      'Youssef Elkbiri veut se connecter avec vous.',            '{"requestId": 1, "userId": 3}',                       true,  NOW() - INTERVAL '48 days', NOW() - INTERVAL '48 days'),
(4,  4,  'POST_LIKED',          'Post aimé',             'Votre question sur les intégrales a reçu un nouveau like.','{"postId": 3, "userId": 5}',                          true,  NOW() - INTERVAL '29 days', NOW() - INTERVAL '29 days'),
(5,  4,  'QUESTION_ANSWERED',   'Nouvelle réponse',      'Youssef a répondu à votre question sur les équations diff.','{"questionId": 1, "answerId": 1, "userId": 3}',       true,  NOW() - INTERVAL '37 days', NOW() - INTERVAL '37 days'),
(6,  4,  'ANSWER_ACCEPTED',     'Réponse acceptée ✓',   'Votre réponse a été marquée comme solution!',             '{"questionId": 2, "answerId": 4}',                    false, NULL,                        NOW() - INTERVAL '30 days'),
(7,  5,  'BADGE_EARNED',        'Nouveau badge 🏅',     'Vous avez gagné le badge "Aide Précieuse"!',              '{"badgeId": 2, "badgeName": "Aide Précieuse"}',       false, NULL,                        NOW() - INTERVAL '14 days'),
(8,  5,  'LEVEL_UP',            'Niveau supérieur! 🎉', 'Vous avez atteint le niveau 6! Continuez comme ça.',     '{"newLevel": 6, "points": 1120}',                     false, NULL,                        NOW() - INTERVAL '10 days'),
(9,  9,  'CONNECTION_REQUEST',  'Nouvelle demande',      'Hamza Ait veut se connecter avec vous.',                  '{"requestId": 8, "userId": 9}',                       false, NULL,                        NOW() - INTERVAL '3 days'),
(10, 10, 'CONNECTION_REQUEST',  'Nouvelle demande',      'Siham Berrada veut se connecter avec vous.',              '{"requestId": 9, "userId": 10}',                      false, NULL,                        NOW() - INTERVAL '2 days'),
(11, 3,  'POINTS_EARNED',       'Points gagnés ⭐',     'Vous avez gagné 50 points pour votre réponse acceptée!', '{"points": 50, "reason": "Réponse acceptée"}',        true,  NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days'),
(12, 6,  'POST_COMMENTED',      'Nouveau commentaire',   'Quelqu''un a commenté votre discussion sur la chimie.',  '{"postId": 6, "commentId": 5, "userId": 7}',          false, NULL,                        NOW() - INTERVAL '29 days'),
(13, 8,  'NEW_MESSAGE',         'Nouveau message',       'Fatima Zhra vous a envoyé un message.',                  '{"senderId": 6, "conversationId": "6-8"}',            true,  NOW() - INTERVAL '26 days', NOW() - INTERVAL '26 days'),
(14, 3,  'BADGE_EARNED',        'Nouveau badge 🏅',     'Vous avez gagné le badge "Expert Programmation"!',       '{"badgeId": 3, "badgeName": "Expert Programmation"}', true,  NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days');

SELECT setval(pg_get_serial_sequence('notifications', 'id'), 14);

-- =============================================================
-- 19. MODERATION LOGS
-- =============================================================
INSERT INTO moderation_logs (id, moderator_id, action_type, target_type, target_id, target_user_id, reason, content_snapshot, created_at) VALUES
(1, 2, 'POST_HIDDEN',   'POST',      10, 5, 'Contenu offensant signalé par plusieurs utilisateurs', 'Ce post contenait du contenu inapproprié et offensant.', NOW() - INTERVAL '9 days'),
(2, 1, 'MODERATOR_CREATED', 'MODERATOR', 2, 2, 'Nomination modérateur après 2 mois d''activité',   NULL,                                                    NOW() - INTERVAL '60 days'),
(3, 2, 'USER_DEACTIVATED',  'USER',   11, 11, 'Compte non vérifié après 7 jours',                  NULL,                                                    NOW() - INTERVAL '1 day');

SELECT setval(pg_get_serial_sequence('moderation_logs', 'id'), 3);

-- =============================================================
-- 20. PLATFORM STATS (single-row snapshot)
-- =============================================================
INSERT INTO platform_stats (id, total_users, active_users_last_7_days, active_users_last_30_days, new_users_this_week, new_users_this_month, total_posts, total_questions, total_answers, total_comments, posts_this_week, total_tasks, completed_tasks, total_connections, total_points_awarded, total_badges_earned, last_updated, week_reset_at, month_reset_at) VALUES
(1, 12, 8, 11, 1, 3, 10, 5, 8, 12, 2, 0, 0, 7, 6120, 13, NOW(), NOW() - INTERVAL '7 days', NOW() - INTERVAL '30 days');

-- =============================================================
-- Re-enable FK checks
-- =============================================================
SET session_replication_role = DEFAULT;

-- =============================================================
-- Verify row counts
-- =============================================================
SELECT 'users'                 AS table_name, COUNT(*) FROM users
UNION ALL SELECT 'student_subject',            COUNT(*) FROM student_subject
UNION ALL SELECT 'user_profiles',              COUNT(*) FROM user_profiles
UNION ALL SELECT 'user_profile_subject',       COUNT(*) FROM user_profile_subject
UNION ALL SELECT 'moderator_permissions',      COUNT(*) FROM moderator_permissions
UNION ALL SELECT 'moderator_permission_list',  COUNT(*) FROM moderator_permission_list
UNION ALL SELECT 'connection_requests',        COUNT(*) FROM connection_requests
UNION ALL SELECT 'connections',                COUNT(*) FROM connections
UNION ALL SELECT 'community_posts',            COUNT(*) FROM community_posts
UNION ALL SELECT 'community_questions',        COUNT(*) FROM community_questions
UNION ALL SELECT 'community_answers',          COUNT(*) FROM community_answers
UNION ALL SELECT 'community_comments',         COUNT(*) FROM community_comments
UNION ALL SELECT 'post_likes',                 COUNT(*) FROM post_likes
UNION ALL SELECT 'answer_votes',               COUNT(*) FROM answer_votes
UNION ALL SELECT 'messages',                   COUNT(*) FROM messages
UNION ALL SELECT 'badges',                     COUNT(*) FROM badges
UNION ALL SELECT 'user_scores',                COUNT(*) FROM user_scores
UNION ALL SELECT 'user_badges',                COUNT(*) FROM user_badges
UNION ALL SELECT 'notifications',              COUNT(*) FROM notifications
UNION ALL SELECT 'moderation_logs',            COUNT(*) FROM moderation_logs
UNION ALL SELECT 'platform_stats',             COUNT(*) FROM platform_stats;
