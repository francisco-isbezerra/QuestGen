<?php
/**
 * buscar_desafio.php — QuestGen API v2
 * ─────────────────────────────────────────────────────────────────────────────
 * Aceita POST com os campos: usuario_id e jogo_id
 * ─────────────────────────────────────────────────────────────────────────────
 */

header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");

define("DB_HOST",    "localhost");
define("DB_NAME",    "questgen");
define("DB_USER",    "root");
define("DB_PASS",    "");
define("DB_CHARSET", "utf8mb4");

try {
    $dsn = "mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=" . DB_CHARSET;
    $pdo = new PDO($dsn, DB_USER, DB_PASS, [
        PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES   => false,
    ]);
} catch (PDOException $e) {
    echo json_encode([
        "status"   => "Erro",
        "mensagem" => "Erro de conexão com o banco de dados: " . $e->getMessage()
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

if ($_SERVER["REQUEST_METHOD"] === "POST") {
    $usuarioId = isset($_POST["usuario_id"]) ? (int) $_POST["usuario_id"] : 0;
    $jogoId    = isset($_POST["jogo_id"])    ? (int) $_POST["jogo_id"]    : -1;

    if ($usuarioId <= 0) {
        echo json_encode(["status" => "Erro", "mensagem" => "usuario_id inválido ou ausente."]);
        exit;
    }
    if ($jogoId < 0) {
        echo json_encode(["status" => "Erro", "mensagem" => "jogo_id inválido ou ausente."]);
        exit;
    }

    try {
        // ══════════════════════════════════════════════════════════════════════════
        // MODO HOME — jogo_id == 0
        // ══════════════════════════════════════════════════════════════════════════
        if ($jogoId === 0) {
            $stmt = $pdo->prepare("
                SELECT
                    u.USUARIO_ID          AS usuarioId,
                    u.USUARIO_NOME        AS usuarioNome,
                    u.USUARIO_EMAIL       AS usuarioEmail,
                    u.GAMECOINS           AS gameCoins,
                    u.WIN_RATE            AS winRate,
                    u.RANK_NOME           AS rankNome,
                    d.DESAFIO_ID          AS desafioId,
                    d.JOGO_ID             AS jogoId,
                    d.DESAFIO_TITULO      AS titulo,
                    d.DESAFIO_DESCRICAO   AS descricao,
                    d.DESAFIO_RECOMPENSA  AS recompensa,
                    d.DESAFIO_DIFICULDADE AS dificuldade,
                    ud.STATUS             AS statusDesafio
                FROM USUARIO u
                LEFT JOIN USUARIO_DESAFIO ud
                    ON ud.USUARIO_ID = u.USUARIO_ID
                    AND ud.STATUS = 'EM_CURSO'
                LEFT JOIN DESAFIO d
                    ON d.DESAFIO_ID = ud.DESAFIO_ID
                WHERE u.USUARIO_ID = :uid
                ORDER BY ud.UD_ID DESC
                LIMIT 1
            ");
            $stmt->execute([":uid" => $usuarioId]);
            $linha = $stmt->fetch();

            if (!$linha) {
                echo json_encode(["status" => "Erro", "mensagem" => "Usuário não encontrado."]);
                exit;
            }

            // Converter tipos numéricos
            $linha["usuarioId"] = (int) $linha["usuarioId"];
            $linha["gameCoins"] = (int) $linha["gameCoins"];
            if (isset($linha["winRate"])) {
                $linha["winRate"] = (int) $linha["winRate"];
            }

            if ($linha["desafioId"] !== null) {
                $linha["desafioId"]   = (int) $linha["desafioId"];
                $linha["jogoId"]      = (int) $linha["jogoId"];
                $linha["recompensa"]  = (int) $linha["recompensa"];
                $linha["dificuldade"] = (int) $linha["dificuldade"];
            }

            // Para garantir que o JSON tenha tanto statusDesafio quanto status para o desafio
            $linha["statusDesafio"] = $linha["statusDesafio"] ?? null;
            $linha["status"]        = $linha["statusDesafio"];

            // Retorna o perfil mais o desafio (se houver) no mesmo JSON
            echo json_encode(array_merge(["status" => "Sucesso"], $linha), JSON_UNESCAPED_UNICODE);
            exit;
        }

        // ══════════════════════════════════════════════════════════════════════════
        // MODO DETALHES — jogo_id > 0
        // ══════════════════════════════════════════════════════════════════════════

        // 1. Busca se já há um desafio em andamento (EM_CURSO ou CONCLUIDO) para este jogo e usuário
        $stmtAtivo = $pdo->prepare("
            SELECT
                d.DESAFIO_ID          AS desafioId,
                d.JOGO_ID             AS jogoId,
                d.DESAFIO_TITULO      AS titulo,
                d.DESAFIO_DESCRICAO   AS descricao,
                d.DESAFIO_RECOMPENSA  AS recompensa,
                d.DESAFIO_DIFICULDADE AS dificuldade,
                ud.STATUS             AS statusDesafio
            FROM USUARIO_DESAFIO ud
            JOIN DESAFIO d ON d.DESAFIO_ID = ud.DESAFIO_ID
            WHERE ud.USUARIO_ID = :uid
              AND d.JOGO_ID     = :jid
              AND ud.STATUS    NOT IN ('REIVINDICADO')
            ORDER BY ud.UD_ID DESC
            LIMIT 1
        ");
        $stmtAtivo->execute([":uid" => $usuarioId, ":jid" => $jogoId]);
        $ativo = $stmtAtivo->fetch();

        if ($ativo) {
            $ativo["desafioId"]   = (int) $ativo["desafioId"];
            $ativo["jogoId"]      = (int) $ativo["jogoId"];
            $ativo["recompensa"]  = (int) $ativo["recompensa"];
            $ativo["dificuldade"] = (int) $ativo["dificuldade"];
            $ativo["statusDesafio"] = $ativo["statusDesafio"];
            $ativo["status"]      = $ativo["statusDesafio"];

            echo json_encode($ativo, JSON_UNESCAPED_UNICODE);
            exit;
        }

        // 2. Se não houver desafio ativo, sorteia um desafio novo que o usuário ainda não tentou/concluiu
        $stmtNovo = $pdo->prepare("
            SELECT
                d.DESAFIO_ID          AS desafioId,
                d.JOGO_ID             AS jogoId,
                d.DESAFIO_TITULO      AS titulo,
                d.DESAFIO_DESCRICAO   AS descricao,
                d.DESAFIO_RECOMPENSA  AS recompensa,
                d.DESAFIO_DIFICULDADE AS dificuldade
            FROM DESAFIO d
            WHERE d.JOGO_ID = :jid
              AND d.DESAFIO_ID NOT IN (
                  SELECT DESAFIO_ID
                  FROM USUARIO_DESAFIO
                  WHERE USUARIO_ID = :uid
              )
            ORDER BY RAND()
            LIMIT 1
        ");
        $stmtNovo->execute([":jid" => $jogoId, ":uid" => $usuarioId]);
        $novo = $stmtNovo->fetch();

        if ($novo) {
            $novo["desafioId"]     = (int) $novo["desafioId"];
            $novo["jogoId"]        = (int) $novo["jogoId"];
            $novo["recompensa"]    = (int) $novo["recompensa"];
            $novo["dificuldade"]   = (int) $novo["dificuldade"];
            $novo["statusDesafio"] = null; // Desafio novo, não aceito ainda
            $novo["status"]        = null;

            echo json_encode($novo, JSON_UNESCAPED_UNICODE);
            exit;
        }

        // Caso todos os desafios já tenham sido completados
        echo json_encode([
            "status"    => "SemDesafio",
            "mensagem"  => "Você já completou todos os desafios disponíveis para este jogo!"
        ], JSON_UNESCAPED_UNICODE);

    } catch (PDOException $e) {
        echo json_encode([
            "status"   => "Erro",
            "mensagem" => "Erro no banco de dados: " . $e->getMessage()
        ], JSON_UNESCAPED_UNICODE);
    }
} else {
    echo json_encode([
        "status"   => "Erro",
        "mensagem" => "Método não suportado. Use POST."
    ], JSON_UNESCAPED_UNICODE);
}
